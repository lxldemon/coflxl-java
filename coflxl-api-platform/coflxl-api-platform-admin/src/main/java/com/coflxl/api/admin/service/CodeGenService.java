package com.coflxl.api.admin.service;

import com.coflxl.api.admin.dto.CodeGenRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeGenService {

    public Map<String, String> generateCode(CodeGenRequest request) {
        String ddl = request.getDdl();
        if (ddl == null || ddl.trim().isEmpty()) {
            throw new IllegalArgumentException("DDL不能为空");
        }

        TableInfo tableInfo = parseDdl(ddl);
        tableInfo.setPackageName(request.getPackageName());
        tableInfo.setAuthor(request.getAuthor());

        Map<String, String> result = new HashMap<>();
        try {
            result.put(tableInfo.getClassName() + ".java", generateFromTemplate("entity.java.template", tableInfo));
            result.put(tableInfo.getClassName() + "Controller.java", generateFromTemplate("controller.java.template", tableInfo));
            result.put(tableInfo.getClassName() + "Manage.vue", generateFromTemplate("vue.vue.template", tableInfo));
        } catch (IOException e) {
            throw new RuntimeException("读取模板文件失败", e);
        }

        return result;
    }

    private String generateFromTemplate(String templateName, TableInfo table) throws IOException {
        String template = "";
        try {
            ClassPathResource resource = new ClassPathResource("templates/codegen/" + templateName);
            template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Fallback to file system for development environment
            java.io.File file = new java.io.File("coflxl-api-platform/coflxl-api-platform-admin/src/main/resources/templates/codegen/" + templateName);
            if (!file.exists()) {
                file = new java.io.File("coflxl-api-platform-admin/src/main/resources/templates/codegen/" + templateName);
            }
            if (!file.exists()) {
                file = new java.io.File("src/main/resources/templates/codegen/" + templateName);
            }
            if (file.exists()) {
                template = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            } else {
                throw new IOException("无法找到模板文件: " + templateName, e);
            }
        }

        template = template.replace("${packageName}", table.getPackageName());
        template = template.replace("${author}", table.getAuthor());
        template = template.replace("${tableName}", table.getTableName());
        template = template.replace("${tableComment}", table.getTableComment());
        template = template.replace("${className}", table.getClassName());
        template = template.replace("${objectName}", table.getObjectName());
        template = template.replace("${pathName}", table.getTableName().replace("_", "-"));

        if (templateName.equals("entity.java.template")) {
            StringBuilder columnsStr = new StringBuilder();
            for (ColumnInfo col : table.getColumns()) {
                if ("id".equalsIgnoreCase(col.getColumnName())) {
                    columnsStr.append("    @Id(strategy = \"identity\")\n");
                }
                columnsStr.append("    @Column(name = \"").append(col.getColumnName()).append("\")\n");
                columnsStr.append("    private ").append(col.getJavaType()).append(" ").append(col.getFieldName()).append(";\n\n");
            }
            template = template.replace("${columns}", columnsStr.toString());
        }

        if (templateName.equals("controller.java.template")) {
            StringBuilder searchConditions = new StringBuilder();
            for (ColumnInfo col : table.getColumns()) {
                if (!"id".equalsIgnoreCase(col.getFieldName()) && !"createdAt".equalsIgnoreCase(col.getFieldName()) && !"updatedAt".equalsIgnoreCase(col.getFieldName())) {
                    if ("String".equals(col.getJavaType())) {
                        searchConditions.append("        if (params.get(\"").append(col.getFieldName()).append("\") != null && !params.get(\"").append(col.getFieldName()).append("\").toString().trim().isEmpty()) {\n");
                        searchConditions.append("            sql.append(\" and ").append(col.getColumnName()).append(" like :").append(col.getFieldName()).append(" \");\n");
                        searchConditions.append("            params.put(\"").append(col.getFieldName()).append("\", \"%\" + params.get(\"").append(col.getFieldName()).append("\") + \"%\");\n");
                        searchConditions.append("        }\n");
                    } else {
                        searchConditions.append("        if (params.get(\"").append(col.getFieldName()).append("\") != null && !params.get(\"").append(col.getFieldName()).append("\").toString().trim().isEmpty()) {\n");
                        searchConditions.append("            sql.append(\" and ").append(col.getColumnName()).append(" = :").append(col.getFieldName()).append(" \");\n");
                        searchConditions.append("        }\n");
                    }
                }
            }
            template = template.replace("${searchConditions}", searchConditions.toString());
        }

        if (templateName.equals("vue.vue.template")) {
            StringBuilder formItems = new StringBuilder();
            StringBuilder columnDefs = new StringBuilder();
            StringBuilder searchItems = new StringBuilder();
            for (ColumnInfo col : table.getColumns()) {
                if (!"id".equalsIgnoreCase(col.getFieldName()) && !"createdAt".equalsIgnoreCase(col.getFieldName()) && !"updatedAt".equalsIgnoreCase(col.getFieldName())) {
                    formItems.append("        <el-form-item label=\"").append(col.getComment()).append("\">\n");
                    formItems.append("          <el-input v-model=\"formData.").append(col.getFieldName()).append("\" placeholder=\"请输入").append(col.getComment()).append("\" />\n");
                    formItems.append("        </el-form-item>\n");

                    searchItems.append("          <el-form-item label=\"").append(col.getComment()).append("\">\n");
                    searchItems.append("            <el-input v-model=\"searchForm.").append(col.getFieldName()).append("\" placeholder=\"请输入").append(col.getComment()).append("\" clearable style=\"width: 200px\" />\n");
                    searchItems.append("          </el-form-item>\n");
                }
                columnDefs.append("  { prop: '").append(col.getFieldName()).append("', label: '").append(col.getComment()).append("' },\n");
            }
            template = template.replace("${formItems}", formItems.toString());
            template = template.replace("${columnDefs}", columnDefs.toString());
            template = template.replace("${searchItems}", searchItems.toString());
        }

        return template;
    }

    private TableInfo parseDdl(String ddl) {
        TableInfo tableInfo = new TableInfo();

        // 解析表名
        Pattern tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`?(\\w+)`?", Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(ddl);
        if (tableMatcher.find()) {
            tableInfo.setTableName(tableMatcher.group(1));
            tableInfo.setClassName(toCamelCase(tableInfo.getTableName(), true));
            tableInfo.setObjectName(toCamelCase(tableInfo.getTableName(), false));
        } else {
            throw new IllegalArgumentException("无法解析表名，请检查DDL格式");
        }

        // 解析表注释
        Pattern commentPattern = Pattern.compile("COMMENT\\s*=\s*'([^']*)'", Pattern.CASE_INSENSITIVE);
        Matcher commentMatcher = commentPattern.matcher(ddl);
        if (commentMatcher.find()) {
            tableInfo.setTableComment(commentMatcher.group(1));
        } else {
            tableInfo.setTableComment(tableInfo.getClassName());
        }

        // 解析列
        List<ColumnInfo> columns = new ArrayList<>();
        String[] lines = ddl.split("\n");
        Pattern columnPattern = Pattern.compile("^\\s*`?([a-zA-Z_0-9]+)`?\\s+([a-zA-Z]+)", Pattern.CASE_INSENSITIVE);
        Pattern colCommentPattern = Pattern.compile("COMMENT\\s*['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            String trimLine = line.trim();
            if (trimLine.toUpperCase().startsWith("PRIMARY") ||
                    trimLine.toUpperCase().startsWith("KEY") ||
                    trimLine.toUpperCase().startsWith("UNIQUE") ||
                    trimLine.toUpperCase().startsWith("INDEX") ||
                    trimLine.toUpperCase().startsWith("CONSTRAINT") ||
                    trimLine.toUpperCase().startsWith("CREATE") ||
                    trimLine.startsWith(")")) {
                continue;
            }

            Matcher colMatcher = columnPattern.matcher(trimLine);
            if (colMatcher.find()) {
                ColumnInfo col = new ColumnInfo();
                col.setColumnName(colMatcher.group(1));
                col.setFieldName(toCamelCase(col.getColumnName(), false));
                col.setDbType(colMatcher.group(2).toUpperCase());
                col.setJavaType(convertToJavaType(col.getDbType()));

                Matcher cMatcher = colCommentPattern.matcher(trimLine);
                if (cMatcher.find()) {
                    col.setComment(cMatcher.group(1));
                } else {
                    col.setComment(col.getFieldName());
                }
                columns.add(col);
            }
        }
        tableInfo.setColumns(columns);

        return tableInfo;
    }

    private String toCamelCase(String s, boolean capitalizeFirst) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = capitalizeFirst;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    private String convertToJavaType(String dbType) {
        if (dbType.contains("VARCHAR") || dbType.contains("TEXT") || dbType.contains("CHAR")) return "String";
        if (dbType.contains("BIGINT")) return "Long";
        if (dbType.contains("INT") || dbType.contains("TINYINT")) return "Integer";
        if (dbType.contains("DATETIME") || dbType.contains("TIMESTAMP") || dbType.contains("DATE")) return "LocalDateTime";
        if (dbType.contains("DECIMAL") || dbType.contains("NUMERIC")) return "java.math.BigDecimal";
        return "String";
    }

    static class TableInfo {
        private String tableName;
        private String tableComment;
        private String className;
        private String objectName;
        private String packageName;
        private String author;
        private List<ColumnInfo> columns;
        // getters and setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public String getTableComment() { return tableComment; }
        public void setTableComment(String tableComment) { this.tableComment = tableComment; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getObjectName() { return objectName; }
        public void setObjectName(String objectName) { this.objectName = objectName; }
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public List<ColumnInfo> getColumns() { return columns; }
        public void setColumns(List<ColumnInfo> columns) { this.columns = columns; }
    }

    static class ColumnInfo {
        private String columnName;
        private String fieldName;
        private String dbType;
        private String javaType;
        private String comment;
        // getters and setters
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }
        public String getDbType() { return dbType; }
        public void setDbType(String dbType) { this.dbType = dbType; }
        public String getJavaType() { return javaType; }
        public void setJavaType(String javaType) { this.javaType = javaType; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}
