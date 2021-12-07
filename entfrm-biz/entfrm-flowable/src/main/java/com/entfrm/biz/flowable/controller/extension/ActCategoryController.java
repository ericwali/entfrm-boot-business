package com.entfrm.biz.flowable.controller.extension;

import com.entfrm.base.api.R;
import com.entfrm.biz.flowable.entity.ActCategory;
import com.entfrm.biz.flowable.service.ActCategoryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 *<p>
 * 流程分类管理 controller
 *</p>
 *
 * @Author: entfrm开发团队-王翔
 * @Date: 2021/5/13
 */
@RestController
@RequestMapping("/flowable/extension/actCategory")
@AllArgsConstructor
public class ActCategoryController {

    private final ActCategoryService actCategoryService;


    @GetMapping("/list")
    public R list() {
        List<ActCategory> actCategories = actCategoryService.list();
        return R.ok(actCategories);
    }



    @GetMapping("/{id}")
    public R getById(@PathVariable("id") Integer id) {
        return R.ok(actCategoryService.getById(id));
    }



    @PostMapping("/save")
    public R save(@RequestBody ActCategory actCategory) {
        actCategoryService.save(actCategory);
        return R.ok();
    }


    @PutMapping("/update")
    public R update(@RequestBody ActCategory actCategory) {
        actCategoryService.updateById(actCategory);
        return R.ok();
    }



    @DeleteMapping("/remove/{id}")
    public R remove(@PathVariable Integer[] id) {
        actCategoryService.removeByIds(Arrays.asList(id));
        return R.ok();
    }


}
