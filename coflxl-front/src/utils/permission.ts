export default {
    mounted(el: HTMLElement, binding: any) {
        const permissionsStr = localStorage.getItem('permissions')
        const perms: string[] = permissionsStr ? JSON.parse(permissionsStr) : []
        const value = binding.value

        if (value && typeof value === 'string') {
            if (!perms.includes(value)) {
                el.parentNode && el.parentNode.removeChild(el)
            }
        } else if (value && Array.isArray(value)) {
            const hasPermission = value.some(val => perms.includes(val))
            if (!hasPermission) {
                el.parentNode && el.parentNode.removeChild(el)
            }
        } else {
            throw new Error(`需要权限标识，如 v-permission="'sys:user:add'"`)
        }
    }
}
