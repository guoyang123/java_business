package com.neuedu.service.impl;

import com.google.common.collect.Lists;
import com.neuedu.common.Const;
import com.neuedu.dao.CartMapper;
import com.neuedu.dao.ProductMapper;
import com.neuedu.pojo.Cart;
import com.neuedu.pojo.Product;
import com.neuedu.service.ICartService;
import com.neuedu.utils.BigDecimalUtil;
import com.neuedu.utils.PropertiesUtil;
import com.neuedu.vo.CartProductVO;
import com.neuedu.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements ICartService {


    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取购物车高可用对象CartVO
     * */
    private CartVO getCartVOLimit(Integer userId){
        CartVO cartVO=new CartVO();
        //step1:userId --->List<Cart>
         List<Cart> cartList=cartMapper.findCartsByUserid(userId);
        //step2: List<Cart>-->List<CartProductVO>
        List<CartProductVO> cartProductVOList= Lists.newArrayList();
        BigDecimal totalPrice=new BigDecimal("0");
        if(cartList!=null){
            for(Cart cart:cartList){
                //cart-->CartProductVO
                CartProductVO cartProductVO=new CartProductVO();
                cartProductVO.setId(cart.getId());
                cartProductVO.setUserId(userId);
                cartProductVO.setProductId(cart.getProductId());
                //prodcutid--->product
               Product product= productMapper.selectByPrimaryKey(cart.getProductId());
               if(product!=null){
                   cartProductVO.setProductName(product.getName());
                   cartProductVO.setProductSubtitle(product.getSubtitle());
                   cartProductVO.setProductPrice(product.getPrice());
                   cartProductVO.setProductStatus(product.getStatus());
                   cartProductVO.setProductStock(product.getStock());

                   //判断库存
                   int buyLimitCount=0;
                   if(product.getStock()>cart.getQuantity()){
                       //库存充足
                       cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                       buyLimitCount=cart.getQuantity();
                   }else{
                       //库存不足
                       cartProductVO.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                       buyLimitCount=product.getStock();
                       Cart cart1=new Cart();
                       cart1.setId(cart.getId());
                       cart1.setQuantity(product.getStock());
                       cartMapper.updateByPrimaryKeyBySelectActive(cart1);

                   }
                   cartProductVO.setQuantity(buyLimitCount);
                   BigDecimal cartProucttotalprice=  BigDecimalUtil.mul(product.getPrice().doubleValue(),cart.getQuantity().doubleValue());
                   cartProductVO.setProductTotalPrice(cartProucttotalprice);
                   totalPrice=BigDecimalUtil.add(totalPrice.doubleValue(),cartProucttotalprice.doubleValue());

               }
                cartProductVOList.add(cartProductVO);
            }

        }

        //step3:组装cartvo
        cartVO.setCartProductVOList(cartProductVOList);
        cartVO.setAllChecked(cartMapper.isAllChecked(userId)==0);
        cartVO.setCartTotalPrice(totalPrice);
        cartVO.setImageHost(PropertiesUtil.getProperty("imagehost").toString());

        return cartVO;
    }


}
