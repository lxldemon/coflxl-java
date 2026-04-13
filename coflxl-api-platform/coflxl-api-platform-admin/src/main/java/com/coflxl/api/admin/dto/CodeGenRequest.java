package com.coflxl.api.admin.dto;

public class CodeGenRequest {
    private String ddl;
    private String packageName = "com.coflxl.api";
    private String author = "coflxl";

    public String getDdl() {
        return ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
