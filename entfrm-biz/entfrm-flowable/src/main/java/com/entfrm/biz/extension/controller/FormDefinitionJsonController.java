package com.entfrm.biz.extension.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.entfrm.base.api.R;
import com.entfrm.biz.extension.entity.Button;
import com.entfrm.biz.extension.entity.FormDefinitionJson;
import com.entfrm.biz.extension.entity.FormDefinitionJson;
import com.entfrm.biz.extension.service.FormDefinitionJsonService;
import com.entfrm.biz.extension.service.FormDefinitionJsonService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 *<p>
 * 表单定义设计数据 Controller
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2021/12/4
 */
@RestController
@RequestMapping(value = "/flowable/extension/formDefinitionJson")
@AllArgsConstructor
public class FormDefinitionJsonController {

    private final FormDefinitionJsonService formDefinitionJsonService;

    private LambdaQueryWrapper<FormDefinitionJson> getLambdaQueryWrapper(FormDefinitionJson formDefinitionJson) {
        return new LambdaQueryWrapper<FormDefinitionJson>()
                .eq(FormDefinitionJson::getFormDefinitionId, formDefinitionJson.getFormDefinitionId())
                .eq(ObjectUtil.isNotEmpty(formDefinitionJson.getVersion()), FormDefinitionJson::getVersion, formDefinitionJson.getVersion())
                .eq(StrUtil.isNotBlank(formDefinitionJson.getStatus()), FormDefinitionJson::getStatus, formDefinitionJson.getStatus())
                .eq(StrUtil.isNotBlank(formDefinitionJson.getIsPrimary()), FormDefinitionJson::getIsPrimary, formDefinitionJson.getIsPrimary())
                .orderByDesc(FormDefinitionJson::getCreateTime);
    }

    @GetMapping("/list")
    public R list(Page page, FormDefinitionJson formDefinitionJson) {
        IPage<FormDefinitionJson> result = formDefinitionJsonService.page(page, getLambdaQueryWrapper(formDefinitionJson));
        return R.ok(result.getRecords(), result.getTotal());
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Integer id) {
        return R.ok(formDefinitionJsonService.getById(id));
    }


    @DeleteMapping("/remove/{id}")
    public R remove(@PathVariable Integer[] id) {
        formDefinitionJsonService.removeByIds(Arrays.asList(id));
        return R.ok();
    }

    @PutMapping("/updatePrimaryVersion/{id}")
    public R updatePrimaryVersion(@PathVariable Integer id) {
        FormDefinitionJson formDefinitionJson =formDefinitionJsonService.getById(id);
        //其余版本更新为非主版本
        formDefinitionJsonService.update(
                new FormDefinitionJson()
                        .setIsPrimary("0"),
                new LambdaUpdateWrapper<FormDefinitionJson>()
                        .eq(FormDefinitionJson::getFormDefinitionId, formDefinitionJson.getFormDefinitionId())
        );
        //更新当前版本为主版本
        formDefinitionJson.setIsPrimary("1");
        formDefinitionJsonService.updateById(formDefinitionJson);
        return R.ok();
    }

}
