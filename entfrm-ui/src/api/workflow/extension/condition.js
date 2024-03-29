/**
 * @program: entfrm-ui
 *
 * @description: 流程表达式请求 api
 *
 * @author: entfrm开发团队-王翔
 *
 * @create: 2022/1/15
 */
import request from "@/utils/request";

// 查询流程表达式列表
export function listCondition(query) {
  return request({
    url: '/workflow/extension/condition/list',
    method: 'get',
    params: query
  })
}

// 查询流程表达式详细
export function getCondition(id) {
  return request({
    url: '/workflow/extension/condition/' + id,
    method: 'get'
  })
}

// 新增流程表达式
export function addCondition(data) {
  return request({
    url: '/workflow/extension/condition/save',
    method: 'post',
    data: data
  })
}

// 编辑流程表达式
export function editCondition(data) {
  return request({
    url: '/workflow/extension/condition/update',
    method: 'put',
    data: data
  })
}


// 删除流程表达式
export function delCondition(id) {
  return request({
    url: '/workflow/extension/condition/remove/' + id,
    method: 'delete'
  })
}

