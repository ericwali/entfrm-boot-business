package com.entfrm.biz.toolkit.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.entfrm.base.constant.GenConstants;
import com.entfrm.biz.toolkit.config.GenConfig;
import com.entfrm.biz.toolkit.entity.Column;
import com.entfrm.biz.toolkit.entity.Table;
import org.apache.velocity.VelocityContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class VelocityUtil {
    /**
     * 项目空间路径
     */
    private static final String PROJECT_PATH = "main/java";

    /**
     * mybatis空间路径
     */
    private static final String MYBATIS_PATH = "main/resources/mapper";

    /**
     * 设置模板变量信息
     *
     * @return 模板列表
     */
    public static VelocityContext prepareContext(Table table) {
        //String moduleName = table.getModuleName();
        String businessName = table.getBusinessName();
        String packageName = table.getPackageName();
        String tplCategory = table.getTplCategory();
        List<Column> columnList = JSONUtil.toList(JSONUtil.parseArray(table.getColumns()), Column.class);

        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("tplCategory", table.getTplCategory());
        velocityContext.put("tableName", table.getTableName());
        velocityContext.put("functionName", table.getFunctionName());
        velocityContext.put("ClassName", table.getClassName());
        velocityContext.put("className", StrUtil.lowerFirst(table.getClassName()));
        velocityContext.put("moduleName", table.getModuleName());
        velocityContext.put("businessName", table.getBusinessName());
        velocityContext.put("basePackage", getPackagePrefix(packageName));
        velocityContext.put("packageName", packageName);
        velocityContext.put("author", table.getFunctionAuthor());
        velocityContext.put("datetime", DateUtil.date());
        velocityContext.put("pkColumn", table.getPkColumn());
        velocityContext.put("importList", getImportList(table));
        velocityContext.put("permissionPrefix", businessName);
        velocityContext.put("columns", columnList);
        velocityContext.put("menuId", table.getMenuId());
        velocityContext.put("table", table);
        if (GenConstants.TPL_TREE.equals(tplCategory)) {
            setTreeVelocityContext(velocityContext, table);
        }
        if (GenConstants.TPL_MSUB.equals(tplCategory)) {
            setMSubVelocityContext(velocityContext, table);
        }
        return velocityContext;
    }

    public static void setTreeVelocityContext(VelocityContext context, Table table) {
        String options = table.getOptions();
        JSONObject paramsObj = JSONUtil.parseObj(options);

        String treeId = getTreeId(paramsObj);
        String treeParentId = getTreeParentId(paramsObj);
        String treeName = getTreeName(paramsObj);

        context.put("treeId", treeId);
        context.put("treeParentId", treeParentId);
        context.put("treeName", treeName);
        context.put("expandColumn", getExpandColumn(table));
        if (paramsObj.containsKey(GenConstants.TREE_PARENT_ID)) {
            context.put("tree_parent_id", paramsObj.getStr(GenConstants.TREE_PARENT_ID));
        }
    }

    public static void setMSubVelocityContext(VelocityContext context, Table table) {
        Table subTable = table.getSubTable();
        String subTableName = table.getSubTableName();
        String subTableField = table.getSubTableField();
        String subClassName = table.getSubTable().getClassName();
        String subTableClassField = StrUtil.toCamelCase(subTableField);

        context.put("subTable", subTable);
        context.put("subTableName", subTableName);
        context.put("subTableField", subTableField);
        context.put("subTableClassField", StrUtil.upperFirst(subTableClassField));
        context.put("subTableclassField", subTableClassField);
        context.put("subClassName", subClassName);
        context.put("subclassName", StrUtil.lowerFirst(subClassName));
        context.put("subImportList", getImportList(table.getSubTable()));
    }

    /**
     * 获取模板信息
     *
     * @return 模板列表
     */
    public static List<String> getTemplateList(String tplCategory) {
        List<String> templates = new ArrayList<String>();
        templates.add("templates/java/entity.java.vm");
        templates.add("templates/java/mapper.java.vm");
        templates.add("templates/java/service.java.vm");
        templates.add("templates/java/serviceImpl.java.vm");
        templates.add("templates/java/controller.java.vm");
        templates.add("templates/xml/mapper.xml.vm");
        if (GenConstants.TPL_CRUD.equals(tplCategory) || GenConstants.TPL_MSUB.equals(tplCategory)) {
            templates.add("templates/vue/index.vue.vm");
        } else if (GenConstants.TPL_TREE.equals(tplCategory)) {
            templates.add("templates/vue/index-tree.vue.vm");
        } else if (GenConstants.TPL_MSUB.equals(tplCategory)) {
            templates.add("templates/vue/index.vue.vm");
            templates.add("templates/java/sub-entity.java.vm");
        }
        templates.add("templates/js/api.js.vm");
        templates.add("templates/sql/sql.vm");
        return templates;
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, Table table) {
        // 文件名称
        String fileName = "";
        // 包路径
        String packageName = table.getPackageName();
        // 模块名
        String moduleName = table.getModuleName();
        // 大写类名
        String className = table.getClassName();
        // 业务名称
        String businessName = table.getBusinessName();

        String javaPath = PROJECT_PATH + "/" + StrUtil.replace(packageName, ".", "/");
        String mybatisPath = MYBATIS_PATH + "/" + moduleName;
        String vuePath = "vue";

        if (template.contains("sub-entity.java.vm") && StrUtil.equals(GenConstants.TPL_MSUB, table.getTplCategory())) {
            fileName = StrUtil.format("{}/entity/{}.java", javaPath, table.getSubTable().getClassName());
        }
        if (template.contains("entity.java.vm")) {
            fileName = StrUtil.format("{}/entity/{}.java", javaPath, className);
        } else if (template.contains("mapper.java.vm")) {
            fileName = StrUtil.format("{}/mapper/{}Mapper.java", javaPath, className);
        } else if (template.contains("service.java.vm")) {
            fileName = StrUtil.format("{}/service/{}Service.java", javaPath, className);
        } else if (template.contains("serviceImpl.java.vm")) {
            fileName = StrUtil.format("{}/service/impl/{}ServiceImpl.java", javaPath, className);
        } else if (template.contains("controller.java.vm")) {
            fileName = StrUtil.format("{}/controller/{}Controller.java", javaPath, className);
        } else if (template.contains("mapper.xml.vm")) {
            fileName = StrUtil.format("{}/{}Mapper.xml", mybatisPath, className);
        } else if (template.contains("index.vue.vm")) {
            fileName = StrUtil.format("{}/views/{}/{}/index.vue", vuePath, moduleName, businessName);
        } else if (template.contains("index-tree.vue.vm")) {
            fileName = StrUtil.format("{}/views/{}/{}/index.vue", vuePath, moduleName, businessName);
        } else if (template.contains("api.js.vm")) {
            fileName = StrUtil.format("{}/api/{}/{}.js", vuePath, moduleName, businessName);
        } else if (template.contains("sql.vm")) {
            fileName = businessName + "Menu.sql";
        }
        return fileName;
    }

    /**
     * 获取项目文件路径
     *
     * @return 路径
     */
    public static String getProjectPath() {
        String packageName = GenConfig.getPackageName();
        StringBuffer projectPath = new StringBuffer();
        projectPath.append("main/java/");
        projectPath.append(packageName.replace(".", "/"));
        projectPath.append("/");
        return projectPath.toString();
    }

    /**
     * 获取包前缀
     *
     * @param packageName 包名称
     * @return 包前缀名称
     */
    public static String getPackagePrefix(String packageName) {
        int lastIndex = packageName.lastIndexOf(".");
        String basePackage = StrUtil.sub(packageName, 0, lastIndex);
        return basePackage;
    }

    /**
     * 根据列类型获取导入包
     *
     * @param table 表
     * @return 返回需要导入的包列表
     */
    public static HashSet<String> getImportList(Table table) {
        List<Column> columns = table.getColumns();
        Table subTable = table.getSubTable();
        HashSet<String> importList = new HashSet<String>();
        if (!StrUtil.isEmptyIfStr(subTable)) {
            importList.add("java.util.List");
        }
        for (Column column : columns) {
            if (!column.isSuperColumn() && GenConstants.TYPE_DATE.equals(column.getJavaType())) {
                importList.add("java.util.Date");
            } else if (!column.isSuperColumn() && GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType())) {
                importList.add("java.math.BigDecimal");
            }
        }
        return importList;
    }

    /**
     * 获取权限前缀
     *
     * @param moduleName   模块名称
     * @param businessName 业务名称
     * @return 返回权限前缀
     */
    public static String getPermissionPrefix(String moduleName, String businessName) {
        return StrUtil.format("{}_{}", moduleName, businessName);

    }

    /**
     * 获取树编码
     *
     * @param paramsObj 生成其他选项
     * @return 树编码
     */
    public static String getTreeId(JSONObject paramsObj) {
        if (paramsObj.containsKey(GenConstants.TREE_ID)) {
            return StrUtil.toCamelCase(paramsObj.getStr(GenConstants.TREE_ID));
        }
        return "";
    }

    /**
     * 获取树父编码
     *
     * @param paramsObj 生成其他选项
     * @return 树父编码
     */
    public static String getTreeParentId(JSONObject paramsObj) {
        if (paramsObj.containsKey(GenConstants.TREE_PARENT_ID)) {
            return StrUtil.toCamelCase(paramsObj.getStr(GenConstants.TREE_PARENT_ID));
        }
        return "";
    }

    /**
     * 获取树名称
     *
     * @param paramsObj 生成其他选项
     * @return 树名称
     */
    public static String getTreeName(JSONObject paramsObj) {
        if (paramsObj.containsKey(GenConstants.TREE_NAME)) {
            return StrUtil.toCamelCase(paramsObj.getStr(GenConstants.TREE_NAME));
        }
        return "";
    }

    /**
     * 获取需要在哪一列上面显示展开按钮
     *
     * @param table 业务表对象
     * @return 展开按钮列序号
     */
    public static int getExpandColumn(Table table) {
        String options = table.getOptions();
        JSONObject paramsObj = JSONUtil.parseObj(options);
        String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
        int num = 0;
        List<Column> columns = JSONUtil.toList(JSONUtil.parseArray(table.getColumns()), Column.class);
        for (Column column : columns) {
            if (StrUtil.equals("1", column.getIsList())) {
                num++;
                String columnName = column.getColumnName();
                if (columnName.equals(treeName)) {
                    break;
                }
            }
        }
        return num;
    }
}
