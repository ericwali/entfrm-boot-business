<template>
  <div class="app-container">
    <el-form v-show="showSearch"
             :model="queryParams"
             :inline="true"
             ref="queryForm"
             size="small"
             @keyup.enter.native="handleQuery"
             @submit.native.prevent
    >
      <el-form-item prop="name">
        <el-input v-model="queryParams.name"
                  placeholder="名称"
                  clearable
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary"
                   icon="el-icon-search"
                   size="mini"
                   @click="handleQuery"
        >搜索</el-button>
        <el-button icon="el-icon-refresh-right"
                   size="mini"
                   @click="resetQuery"
        >重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary"
                   size="mini"
                   icon="el-icon-plus"
                   @click="handleAdd"
        >新建</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning"
                   size="mini"
                   icon="el-icon-edit-outline"
                   :disabled="single"
                   @click="handleEdit"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger"
                   size="mini"
                   icon="el-icon-delete"
                   :disabled="multiple"
                   @click="handleDel"
        >删除</el-button>
      </el-col>
      <div class="top-right-btn">
        <el-tooltip class="item"
                    effect="dark"
                    content="刷新"
                    placement="top"
        >
          <el-button size="mini"
                     circle
                     icon="el-icon-refresh"
                     @click="handleQuery"
          />
        </el-tooltip>
        <el-tooltip class="item"
                    effect="dark"
                    :content="showSearch ? '隐藏搜索' : '显示搜索'"
                    placement="top"
        >
          <el-button size="mini"
                     circle icon="el-icon-search"
                     @click="showSearch=!showSearch"
          />
        </el-tooltip>
      </div>
    </el-row>
    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" align="center" width="50"/>
      <el-table-column prop="name" label="名称"/>
      <el-table-column prop="expression" label="表达式"/>
      <el-table-column prop="remarks" label="备注"/>
      <el-table-column align="center" width="200" label="操作">
        <template slot-scope="scope">
          <el-button type="text"
                     icon="el-icon-view"
                     size="mini"
                     @click="handleView(scope.row)"
          >查看</el-button>
          <el-button type="text"
                     icon="el-icon-edit"
                     size="mini"
                     @click="handleEdit(scope.row)"
          >修改</el-button>
          <el-button type="text"
                     size="mini"
                     icon="el-icon-delete"
                     @click="handleDel(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0"
                :total="total"
                :page.sync="queryParams.current"
                :limit.sync="queryParams.size"
                @pagination="getList"
    />
    <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title"
               :visible.sync="open"
               :close-on-click-modal="false"
    >
      <el-form size="small"
               :model="form"
               ref="form"
               label-width="120px"
               :disabled="method=='view'"
               @keyup.enter.native="handleSubmitForm"
               @submit.native.prevent
      >
        <el-row :gutter="15">
          <el-col :span="24">
            <el-form-item label="名称"
                          prop="name"
                          :rules="[{required: true, message:'名称不能为空', trigger:'blur'}]"
            >
              <el-input v-model="form.name" placeholder="请填写名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="表达式"
                          prop="expression"
                          :rules="[{required: true, message:'表达式不能为空', trigger:'blur'}]"
            >
              <el-input v-model="form.expression" placeholder="请填写表达式"/>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remarks" :rules="[]">
              <el-input v-model="form.remarks" type="textarea" placeholder="请填写备注"/>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button size="small"
                   @click="open = false"
        >关闭</el-button>
        <el-button v-if="method != 'view'"
                   size="small"
                   type="primary"
                   @click="handleSubmitForm"
        >确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
/**
 * Copyright © 2020-2021 <a href="http://www.entfrm.com/">entfrm</a> All rights reserved.
 * author entfrm开发团队-王翔
 */
import { listCondition, delCondition, editCondition, addCondition, getCondition } from '@/api/workflow/extension/condition'
export default {
  name: 'Condition',
  data () {
    return {
      queryParams: {
        current: 1,
        size: 10,
        name: undefined
      },
      showSearch: true,
      ids: [],
      single: true,
      multiple: true,
      dataList: [],
      total: 0,
      loading: false,
      title: '',
      open: false,
      form: {},
      method: ''
    }
  },
  created(){
    this.getList()
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true
      listCondition(this.queryParams).then(response => {
        this.dataList = response.data
        this.total = response.total
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    /** 表单重置,主要清除参数配置对话框缓存 */
    reset () {
      this.form = {
        id: undefined,
        name: undefined,
        expression: undefined,
        remarks: undefined
      }
    },
    /** 处理搜索按钮操作 */
    handleQuery() {
      this.queryParams.current = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery () {
      this.resetForm("queryForm")
    },
    /** 处理多选框选中数据 */
    handleSelectionChange (selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    /** 处理新增按钮操作 */
    handleAdd () {
      this.reset()
      this.title = '添加流程表达式'
      this.method = 'add'
      this.open = true
    },
    /** 处理修改按钮操作 */
    handleEdit (row) {
      const id = row.id || this.ids
      getCondition(id).then(response => {
        this.form = response.data
        this.title = '修改流程表达式'
        this.method = 'edit'
        this.open = true
      })
    },
    /** 处理查看按钮操作 */
    handleView (row) {
      const id = row.id || this.ids
      getCondition(id).then(response => {
        this.form = response.data
        this.title = "查看流程表达式"
        this.method = 'view'
        this.open = true
      })
    },
    /** 处理删除按钮操作 */
    handleDel (row) {
      const ids = row.id || this.ids
      this.$confirm('是否确认删除所选项编号为"' + ids + '"的数据项?', "警告", {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.loading = true
        return delCondition(ids).then(response => {
          this.msgSuccess("删除成功")
          this.getList()
        })
      }).catch(() => { this.loading = false })
    },
    /** 处理表单提交 */
    handleSubmitForm () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.method == 'edit') {
            editCondition(this.form).then(response => {
              if (response.code === 0) {
                this.msgSuccess("修改成功")
                this.open = false
                this.getList()
              } else {
                this.msgError(response.msg)
              }
            })
          } else {
            addCondition(this.form).then(response => {
              if (response.code === 0) {
                this.msgSuccess("新增成功")
                this.open = false
                this.getList()
              } else {
                this.msgError(response.msg)
              }
            })
          }
        }
      })
    }
  }
}
</script>
