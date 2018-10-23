package com.neuedu.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
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
                   cartProductVO.setProductChecked(cart.getChecked());
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
                   if(cart.getChecked()==Const.Cart.CHECKED){
                       totalPrice=BigDecimalUtil.add(totalPrice.doubleValue(),cartProucttotalprice.doubleValue());
                   }

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


    @Override
    public ServerResponse add(Integer userid, Integer productId, Integer count) {
        //step1:非空判断
        if(productId==null||count==null){
            return ServerResponse.createByError("参数错误");
        }
        //step2:根据productid查询Cart
      Cart cart=  cartMapper.findCartByProductIdAndUserid(productId,userid);
        if(cart==null){//购物车中没有改商品信息
            Cart cartItem=new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userid);
            cartMapper.insert(cartItem);
        }else{//购物车已经存在改商品，需要更新商品的数量

            cart.setQuantity(cart.getQuantity()+count);
            cartMapper.updateByPrimaryKeyBySelectActive(cart);
        }
        CartVO cartVO=getCartVOLimit(userid);
        return ServerResponse.createBySuccess(cartVO);
    }

    @Override
    public ServerResponse list(Integer userid) {

        CartVO cartVO=getCartVOLimit(userid);

        return ServerResponse.createBySuccess(cartVO);
    }

    @Override
    public ServerResponse update(Integer userid, Integer productId, Integer count) {
        //step1:非空判断
        if(productId==null||count==null){
            return ServerResponse.createByError("参数错误");
        }
        Cart cart=cartMapper.findCartByProductIdAndUserid(productId,userid);
        if(cart!=null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeyBySelectActive(cart);
        CartVO cartVO=this.getCartVOLimit(userid);
        return ServerResponse.createBySuccess(cartVO);
    }

    @Override
    public ServerResponse delete(Integer userid, String productIds) {
        //step1:非空判断
        if(productIds==null||productIds.equals("")){
            return ServerResponse.createByError("参数错误");
        }
       List<String> productIdList= Splitter.on(",").splitToList(productIds);
        if(productIdList==null||productIdList.size()<=0){
            return ServerResponse.createByError("参数错误");
        }
        cartMapper.deleteByUseridAndProductIds(productIdList,userid);
        CartVO cartVO=getCartVOLimit(userid);

        return ServerResponse.createBySuccess(cartVO);
    }

    @Override
    public ServerResponse selectOrUnselect(Integer userid,Integer productId, Integer checked) {

        cartMapper.checkedOrUncheckedProduct(userid,productId,checked);
        CartVO cartVO=getCartVOLimit(userid);
        return ServerResponse.createBySuccess(cartVO);
    }

    @Override
    public ServerResponse<Integer> get_cart_product_count(Integer userid) {
        int count= cartMapper.selectCartProductCount(userid);
        return ServerResponse.createBySuccess(count);
    }
}
