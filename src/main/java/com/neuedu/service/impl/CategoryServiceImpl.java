package com.neuedu.service.impl;

import com.neuedu.common.ServerResponse;
import com.neuedu.dao.CategoryMapper;
import com.neuedu.pojo.Category;
import com.neuedu.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(int parentId, String categoryName) {

        if(categoryName==null||categoryName.equals("")){
            return ServerResponse.createByError("参数错误");
        }

        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);
      int result= categoryMapper.insert(category);

      if(result>0){
          return  ServerResponse.createBySuccess("添加类别成功");
      }else{
          return ServerResponse.createByError("添加类别失败");
      }
    }

    @Override
    public ServerResponse getCategory(int categoryId) {


        List<Category> categoryList=categoryMapper.findChildCategoryByCategoryId(categoryId);
        return ServerResponse.createBySuccess("成功",categoryList);
    }

    @Override
    public ServerResponse set_category_name(Integer categoryId, String categoryName) {

        if(categoryId==null||categoryName==null){
            return  ServerResponse.createByError("参数错误");
        }
        Category category=new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int result=categoryMapper.updateByPrimaryKey(category);
        if(result>0){
            return ServerResponse.createBySuccess("修改成功");
        }else{
            return ServerResponse.createByError("修改失败");
        }

    }

    @Override
    public ServerResponse get_deep_category(Integer categoryId) {

        Set<Category> categorySet=new HashSet<>();
        Set<Category> categories=findChildCategory(categorySet,categoryId);
        return ServerResponse.createBySuccess("成功",categories);
    }

    private Set<Category>  findChildCategory(Set<Category> categorySet,Integer categoryId){
        //step1：根据categoryId查询本类别
        Category category=  categoryMapper.selectByPrimaryKey(categoryId);
        if(category!=null){
            categorySet.add(category);
        }
        List<Category> categoryList=categoryMapper.findChildCategoryByCategoryId(categoryId);
        for(Category category1:categoryList){
            findChildCategory(categorySet,category1.getId());
        }
        return  categorySet;

    }

}
