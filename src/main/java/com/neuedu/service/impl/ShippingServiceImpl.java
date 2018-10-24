package com.neuedu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.ShippingMapper;
import com.neuedu.pojo.Shipping;
import com.neuedu.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    ShippingMapper shippingMapper;
    @Override
    public ServerResponse add(Integer userid, Shipping shipping) {

        if(shipping==null){
            return  ServerResponse.createByError("参数错误");
        }
         shipping.setUser_id(userid);
        int result=shippingMapper.add(shipping);
        if(result>0){
            Map<String,Integer> map= Maps.newHashMap();
            map.put("shippingId",shipping.getId());

            return ServerResponse.createBySuccess(map);
        }
        return ServerResponse.createByError("添加失败");

    }

    @Override
    public ServerResponse delete(Integer userid, Integer shippingId) {

        if(shippingId==null){
            return  ServerResponse.createByError("shippingId必须");
        }

       int result= shippingMapper.deleteByPrimaryKey(shippingId,userid);
        if(result>0){
            return ServerResponse.createBySuccess("删除成功");
        }
        return ServerResponse.createByError("删除失败");
    }

    @Override
    public ServerResponse update(Integer userid, Shipping shipping) {
        if(shipping==null){
            return  ServerResponse.createByError("参数错误");
        }
        //防止横向越权
        shipping.setUser_id(userid);
       int result= shippingMapper.update(shipping);
        if(result>0){
            return ServerResponse.createBySuccess("地址更新成功");
        }
        return ServerResponse.createByError("地址更新失败");
    }

    @Override
    public ServerResponse select(Integer userid, Integer shippingId) {
        if(shippingId==null){
            return  ServerResponse.createByError("参数错误");
        }
      Shipping shipping=  shippingMapper.selectByIdAndUserid(shippingId,userid);
        if(shipping!=null){
            return ServerResponse.createBySuccess(shipping);
        }
        return ServerResponse.createByError("没有改地址");
    }

    @Override
    public ServerResponse list(Integer userId, Integer pageNo, Integer pageSize) {

        PageHelper.startPage(pageNo,pageSize);
        List<Shipping> shippingList=shippingMapper.selectAll(userId);
        PageInfo pageInfo=new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
