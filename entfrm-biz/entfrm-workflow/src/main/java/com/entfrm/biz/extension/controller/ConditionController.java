package com.entfrm.biz.extension.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.entfrm.base.api.R;
import com.entfrm.biz.extension.entity.Button;
import com.entfrm.biz.extension.entity.Condition;
import com.entfrm.biz.extension.service.ConditionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;


/**
 *<p>
 * 流程表达式 controller
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2021/5/13
 */
@RestController
@RequestMapping("/workflow/extension/condition")
@AllArgsConstructor
public class ConditionController {

    private final ConditionService conditionService;

    private LambdaQueryWrapper<Condition> getLambdaQueryWrapper(Condition condition) {
        return new LambdaQueryWrapper<Condition>()
                .like(StrUtil.isNotBlank(condition.getName()), Condition::getName, condition.getName());
    }

    @GetMapping("/list")
    public R list(Page page, Condition condition) {
        IPage<Condition> result = conditionService.page(page, getLambdaQueryWrapper(condition));
        return R.ok(result.getRecords(), result.getTotal());
    }

    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Integer id) {
        return R.ok(conditionService.getById(id));
    }

    @PostMapping("/save")
    public R save(@RequestBody Condition condition) {
        conditionService.save(condition);
        return R.ok();
    }

    @PutMapping("/update")
    public R update(@RequestBody Condition condition) {
        conditionService.updateById(condition);
        return R.ok();
    }

    @DeleteMapping("/remove/{ids}")
    public R remove(@PathVariable Integer[] ids) {
        conditionService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

}
