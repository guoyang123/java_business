package com.neuedu.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.domain.TradeFundBill;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.MonitorHeartbeatSynResponse;
import com.alipay.demo.trade.DemoHbRunner;
import com.alipay.demo.trade.Main;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.*;
import com.alipay.demo.trade.model.hb.*;
import com.alipay.demo.trade.model.result.AlipayF2FPayResult;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.model.result.AlipayF2FQueryResult;
import com.alipay.demo.trade.model.result.AlipayF2FRefundResult;
import com.alipay.demo.trade.service.AlipayMonitorService;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayMonitorServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeWithHBServiceImpl;
import com.alipay.demo.trade.utils.Utils;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.neuedu.common.Const;
import com.neuedu.common.ServerResponse;
import com.neuedu.dao.*;
import com.neuedu.pojo.*;
import com.neuedu.pojo.Product;
import com.neuedu.service.IOrderService;
import com.neuedu.utils.BigDecimalUtil;
import com.neuedu.utils.DateUtil;
import com.neuedu.utils.FTPUtils;
import com.neuedu.utils.PropertiesUtil;
import com.neuedu.vo.OrderItemVO;
import com.neuedu.vo.OrderProductVO;
import com.neuedu.vo.OrderVO;
import com.neuedu.vo.ShippingVO;
import com.sun.org.apache.xpath.internal.operations.Or;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductMapper productMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    ShippingMapper shippingMapper;
    @Autowired
    PayInfoMapper payInfoMapper;

    @Override
    public ServerResponse createOrder(Integer userid, Integer shippingid) {

        //step1:参数校验
        if(shippingid==null){
            return  ServerResponse.createByError("地址必须选择");
        }
        //step2:查询购物车中勾选的商品--》List<Cart>
         List<Cart> cartList=  cartMapper.selectCheckedCartByUserid(userid);

        //step3:List<Cart> --->List<OrderItem>
        ServerResponse serverResponse=getCartOrderItem(userid,cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        //step4:创建Order并保存订单
            List<OrderItem> orderItemList=(List<OrderItem>)serverResponse.getData();
           BigDecimal orderTotalPrice= getOrderTotalPrice(orderItemList);
         Order order= assembleOrder(userid,shippingid,orderTotalPrice);
         if(order==null){
             return  ServerResponse.createByError("生成订单失败");
         }
        //step5: 批量插入List<OrderItem>
          if(orderItemList==null||orderItemList.size()==0){
             return ServerResponse.createByError("购物车空");
          }
          for(OrderItem orderItem:orderItemList){
             orderItem.setOrderNo(order.getOrderNo());
          }
          //订单明细批量插入mybatis
          orderItemMapper.batchInsertOrderItem(orderItemList);
        //step6:减商品库存
        reduceProductStock(orderItemList);
        //step7:清空已购买的购物车商品
        cleanCart(cartList);
        //step8: 返回OrderVO
       OrderVO orderVO= assembleOrderVO(order,orderItemList);

        return ServerResponse.createBySuccess(orderVO);
    }



    private OrderVO assembleOrderVO(Order order,List<OrderItem> orderItemList){
        OrderVO orderVO=new OrderVO();
        orderVO.setOrderNo(order.getOrderNo());
        orderVO.setPayment(order.getPayment());
        orderVO.setPaymentType(order.getPaymentType());
        orderVO.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getDesc());
        orderVO.setPostage(0);
        orderVO.setStatus(order.getStatus());
        orderVO.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getDesc());
        orderVO.setShippingId(order.getShippingId());
       Shipping shipping= shippingMapper.selectByIdAndUserid(order.getShippingId(),order.getUserId());
       if(shipping!=null){
           orderVO.setReceiverName(shipping.getReceiver_name());
           orderVO.setShippingVO(assembleShippingVO(shipping));
       }
       orderVO.setPaymentTime(DateUtil.dateToStr(order.getPaymentTime()));
        orderVO.setSendTime(DateUtil.dateToStr(order.getSendTime()));
        orderVO.setEndTime(DateUtil.dateToStr(order.getEndTime()));
        orderVO.setCreateTime(DateUtil.dateToStr(order.getCreateTime()));
        orderVO.setCloseTime(DateUtil.dateToStr(order.getCloseTime()));
        orderVO.setImageHost((String) PropertiesUtil.getProperty("imagehost"));

        List<OrderItemVO> orderItemVOList=Lists.newArrayList();
       if(orderItemList!=null){
           for(OrderItem orderItem:orderItemList){
               OrderItemVO orderItemVO=assembleOrderItemVO(orderItem);
               orderItemVOList.add(orderItemVO);
           }
       }

        orderVO.setOrderItemVOList(orderItemVOList);

        return  orderVO;
    }

    /**
     * orderItem-->orderItemVO
     * */
    private  OrderItemVO assembleOrderItemVO(OrderItem orderItem){
        OrderItemVO orderItemVO=new OrderItemVO();
        orderItemVO.setOrderNo(orderItem.getOrderNo());
        orderItemVO.setProductId(orderItem.getProductId());
        orderItemVO.setProductName(orderItem.getProductName());
        orderItemVO.setProductImage(orderItem.getProductImage());
        orderItemVO.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVO.setQuantity(orderItem.getQuantity());
        orderItemVO.setTotalPricel(orderItem.getTotalPrice());
        orderItemVO.setCreateTime(DateUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVO;
    }
    /**
     * shipping-->shippingVO
     * */
    private ShippingVO assembleShippingVO(Shipping shipping){
        ShippingVO shippingVO=new ShippingVO();
        shippingVO.setReceiver_address(shipping.getReceiver_address());
        shippingVO.setReceiver_city(shipping.getReceiver_city());
        shippingVO.setReceiver_district(shipping.getReceiver_district());
        shippingVO.setReceiver_mobile(shipping.getReceiver_mobile());
        shippingVO.setReceiver_name(shipping.getReceiver_name());
        shippingVO.setReceiver_mobile(shipping.getReceiver_mobile());
        shippingVO.setReceiver_province(shipping.getReceiver_province());
        shippingVO.setReceiver_zip(shipping.getReceiver_zip());

        return  shippingVO;
    }
    /**
     * 清空购物车
     * */
    private void cleanCart(List<Cart> cartList){
        for(Cart cart:cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    /**
     * 商品减少库存
     * */
    private void  reduceProductStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem:orderItemList){
           Product product= productMapper.selectByPrimaryKey(orderItem.getProductId());
           product.setStock(product.getStock()-orderItem.getQuantity());
           productMapper.updateByPrimaryKeySelective(product);
        }
    }
    /**
     * 生成订单
     * */
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment){
        Order order=new Order();
        order.setOrderNo(generateOrderNo());
        order.setStatus(Const.OrderStatusEnum.ORDER_UN_PAY.getStatus());
        order.setPostage(0);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.PAY_ON_LINE.getStatus());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        //订单支付时间 发货时间
        int result=orderMapper.insert(order);
        if(result>0){
            return order;
        }
        return null;
    }
    /**
     * 生成订单号
     * */
    private Long  generateOrderNo(){
        long currentTime=System.currentTimeMillis();
        return  currentTime+new Random().nextInt(100);
    }

    /**
     * 计算订单的总价格
     * */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList){
        BigDecimal totalPrice=new BigDecimal("0");
        for(OrderItem orderItem:orderItemList){
            totalPrice= BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return  totalPrice;
    }

    /**
     * 将购物车的购物信息转成订单明细信息
     * */
    private ServerResponse<List<OrderItem>> getCartOrderItem(Integer userId,List<Cart> cartList){
        List<OrderItem> orderItemList= Lists.newArrayList();
        if(cartList==null||cartList.size()==0){
            //购物车空的
            return ServerResponse.createByError("购物车空");
        }
        for(Cart cartItem:cartList){
            OrderItem orderItem=new OrderItem();
            Product product= productMapper.selectByPrimaryKey(cartItem.getProductId());
            if(product==null){
                return  ServerResponse.createByError("商品不存在");
            }
          Integer productStatus=  product.getStatus();
            if(productStatus!= Const.ProductStatusEnum.PRODUCT_ONLLINE.getStatus()){
                //商品下架
                return ServerResponse.createByError("商品"+product.getName()+"已经被下架");
            }
            Integer productStock=product.getStock();
            if(productStock<cartItem.getQuantity()){
                //商品库存不足
                return ServerResponse.createByError("商品"+product.getName()+"库存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity().doubleValue()));
            orderItemList.add(orderItem);
        }


        return ServerResponse.createBySuccess(orderItemList);
    }


    @Override
    public ServerResponse get_order_cart_product(Integer userId) {

        OrderProductVO orderProductVO=new OrderProductVO();

        //购物车获取购物信息
        List<Cart> cartList=   cartMapper.selectCheckedCartByUserid(userId);
       ServerResponse serverResponse= this.getCartOrderItem(userId,cartList);
       if(!serverResponse.isSuccess()){
           return serverResponse;
       }
       List<OrderItem> orderItemList=( List<OrderItem>)serverResponse.getData();
       List<OrderItemVO> orderItemVOList=Lists.newArrayList();
       BigDecimal totalPrice=new BigDecimal("0");
       for(OrderItem orderItem:orderItemList){
           totalPrice=BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
           orderItemVOList.add(assembleOrderItemVO(orderItem));
       }
        orderProductVO.setImageHost((String) PropertiesUtil.getProperty("imagehost"));
       orderProductVO.setOrderItemVOList(orderItemVOList);
       orderProductVO.setProductTotalPrice(totalPrice);

        return ServerResponse.createBySuccess(orderProductVO);
    }

    @Override
    public ServerResponse list(Integer userId,Integer pageNum,Integer pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList=null;
        if(userId==null){
            //管理员
            orderList=orderMapper.selectAll();
        }else{
            orderList=orderMapper.selectAllByUserid(userId);
        }

        List<OrderVO> orderVOList=Lists.newArrayList();
         for(Order order:orderList){
             if(order==null){
                 return ServerResponse.createByError("订单不存在");
             }
             List<OrderItem> orderItemList=orderItemMapper.selectAllByUseridAndOrderNo(userId,order.getOrderNo());
            OrderVO orderVO=assembleOrderVO(order,orderItemList);
             orderVOList.add(orderVO);
         }

        PageInfo pageInfo=new PageInfo(orderVOList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse detail(Integer userId, Long orderNo) {
        if(orderNo==null){
            return  ServerResponse.createByError("订单号必须传递");
        }
        Order order=orderMapper.getOrderByUseridAndOrderNo(userId,orderNo);
        if(order==null){
            return  ServerResponse.createByError("未找到该订单");
        }
      List<OrderItem> orderItemList=  orderItemMapper.selectAllByUseridAndOrderNo(userId,orderNo);
     OrderVO orderVO=   assembleOrderVO(order,orderItemList);

        return ServerResponse.createBySuccess(orderVO);
    }

    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        if(orderNo==null){
            return  ServerResponse.createByError("订单号必须传递");
        }
        Order order=orderMapper.getOrderByUseridAndOrderNo(userId,orderNo);
        if(order==null){
            return  ServerResponse.createByError("未找到该订单");
        }
        if(order.getStatus()!= Const.OrderStatusEnum.ORDER_UN_PAY.getStatus()){
            return  ServerResponse.createByError("已付款订单无法取消");
        }
        Order order1=new Order();
        order1.setId(order.getId());
        order1.setStatus(Const.OrderStatusEnum.ORDER_CANCELLED.getStatus());
       int result= orderMapper.updateOrderBySelectActive(order1);
      if(result>0){
          return ServerResponse.createBySuccess();
      }
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse search(Long orderNo) {
        if(orderNo==null){
            return  ServerResponse.createByError("订单号必须传递");
        }
        Order order=orderMapper.selectOrderByOrderNo(orderNo);
        if(order==null){
            return  ServerResponse.createByError("未找到该订单");
        }
        List<OrderItem> orderItemList=  orderItemMapper.selectAllByUseridAndOrderNo(order.getUserId(),orderNo);
        OrderVO orderVO=   assembleOrderVO(order,orderItemList);
        return ServerResponse.createBySuccess(orderVO);
    }

    @Override
    public ServerResponse send(Long orderNo) {
        if(orderNo==null){
            return  ServerResponse.createByError("订单号必须传递");
        }
        Order order=orderMapper.selectOrderByOrderNo(orderNo);
        if(order==null){
            return  ServerResponse.createByError("未找到该订单");
        }
        if(order.getStatus()==Const.OrderStatusEnum.ORDER_PAYED.getStatus()){
            order.setStatus(Const.OrderStatusEnum.ORDER_SEND.getStatus());
            order.setSendTime(new Date());
           int result= orderMapper.updateOrderBySelectActive(order);
           if(result>0){
               return ServerResponse.createBySuccess();
           }
        }

        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse pay(Integer userId, Long orderNo,String path) {
       //step：参数校验
        if(orderNo==null){
            return  ServerResponse.createByError("订单号不能为空");
        }

        //step2:查询订单
       Order order= orderMapper.getOrderByUseridAndOrderNo(userId,orderNo);
        if(order==null){
            return  ServerResponse.createByError("订单不存在");
        }
        //支付
        // 测试当面付2.0生成支付二维码
      ServerResponse serverResponse=   pay(order,path);

        return serverResponse;
    }

    @Override
    public String alipayCallback(Map<String, String> map) {

        //step1：获取订单号
      String orderNo=  map.get("out_trade_no");
      //订单状态
      String tradeStatus=map.get("trade_status");
     //支付宝流水号
        String tradeNo=map.get("trade_no");
        //查询订单
       Order order= orderMapper.selectOrderByOrderNo(Long.parseLong(orderNo));
       if(order!=null){
           if(tradeStatus.equals("TRADE_SUCCESS")){
               //支付成功
               Order order1=new Order();
               order1.setOrderNo(Long.parseLong(orderNo));
               order1.setStatus(20);//已付款
               order1.setPaymentTime(new Date());
               orderMapper.updateOrderByOrderNo(order1);
               //添加到支付信息表
               PayInfo payInfo=new PayInfo();
               payInfo.setOrderNo(Long.parseLong(orderNo));
               payInfo.setUserId(order.getUserId());
               payInfo.setPayPlatform(Const.PayPlatformEnum.PAY_ALIPAY.getStatus());
               payInfo.setPlatformNumber(tradeNo);
               payInfoMapper.insert(payInfo);
             return "success";
           }
       }
        return "failed";
    }

    @Override
    public ServerResponse query_order_pay_status(Long orderNo) {
        //step1:根据orderNo查询订单
       Order order= orderMapper.selectOrderByOrderNo(orderNo);
       if(order==null){
           return ServerResponse.createByError("订单不存在");
       }
       if(order.getStatus()>=Const.OrderStatusEnum.ORDER_PAYED.getStatus()){

           return ServerResponse.createBySuccess(true);
       }
        return ServerResponse.createBySuccess(false);
    }


    ////////////////////支付///////////////////////////////////////////////////
    private static Log log = LogFactory.getLog(Main.class);

    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
    private static AlipayTradeService   tradeWithHBService;

    // 支付宝交易保障接口服务，供测试接口api使用，请先阅读readme.txt
    private static AlipayMonitorService monitorService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
        tradeWithHBService = new AlipayTradeWithHBServiceImpl.ClientBuilder().build();

        /** 如果需要在程序中覆盖Configs提供的默认参数, 可以使用ClientBuilder类的setXXX方法修改默认参数 否则使用代码中的默认设置 */
        monitorService = new AlipayMonitorServiceImpl.ClientBuilder()
                .setGatewayUrl("http://mcloudmonitor.com/gateway.do").setCharset("GBK")
                .setFormat("json").build();
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }



    // 测试系统商交易保障调度
    public void test_monitor_schedule_logic() {
        // 启动交易保障线程
        DemoHbRunner demoRunner = new DemoHbRunner(monitorService);
        demoRunner.setDelay(5); // 设置启动后延迟5秒开始调度，不设置则默认3秒
        demoRunner.setDuration(10); // 设置间隔10秒进行调度，不设置则默认15 * 60秒
        demoRunner.schedule();

        // 启动当面付，此处每隔5秒调用一次支付接口，并且当随机数为0时交易保障线程退出
        while (Math.random() != 0) {
            test_trade_pay(tradeWithHBService);
            Utils.sleep(5 * 1000);
        }

        // 满足退出条件后可以调用shutdown优雅安全退出
        demoRunner.shutdown();
    }

    // 系统商的调用样例，填写了所有系统商商需要填写的字段
    public void test_monitor_sys() {
        // 系统商使用的交易信息格式，json字符串类型
        List<SysTradeInfo> sysTradeInfoList = new ArrayList<SysTradeInfo>();
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000001", 5.2, HbStatus.S));
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000002", 4.4, HbStatus.F));
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000003", 11.3, HbStatus.P));
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000004", 3.2, HbStatus.X));
        sysTradeInfoList.add(SysTradeInfo.newInstance("00000005", 4.1, HbStatus.X));

        // 填写异常信息，如果有的话
        List<ExceptionInfo> exceptionInfoList = new ArrayList<ExceptionInfo>();
        exceptionInfoList.add(ExceptionInfo.HE_SCANER);
        //        exceptionInfoList.add(ExceptionInfo.HE_PRINTER);
        //        exceptionInfoList.add(ExceptionInfo.HE_OTHER);

        // 填写扩展参数，如果有的话
        Map<String, Object> extendInfo = new HashMap<String, Object>();
        //        extendInfo.put("SHOP_ID", "BJ_ZZ_001");
        //        extendInfo.put("TERMINAL_ID", "1234");

        String appAuthToken = "应用授权令牌";//根据真实值填写

        AlipayHeartbeatSynRequestBuilder builder = new AlipayHeartbeatSynRequestBuilder()
                .setAppAuthToken(appAuthToken).setProduct(com.alipay.demo.trade.model.hb.Product.FP).setType(Type.CR)
                .setEquipmentId("cr1000001").setEquipmentStatus(EquipStatus.NORMAL)
                .setTime(Utils.toDate(new Date())).setStoreId("store10001").setMac("0a:00:27:00:00:00")
                .setNetworkType("LAN").setProviderId("2088911212323549") // 设置系统商pid
                .setSysTradeInfoList(sysTradeInfoList) // 系统商同步trade_info信息
                //                .setExceptionInfoList(exceptionInfoList)  // 填写异常信息，如果有的话
                .setExtendInfo(extendInfo) // 填写扩展信息，如果有的话
                ;

        MonitorHeartbeatSynResponse response = monitorService.heartbeatSyn(builder);
        dumpResponse(response);
    }

    // POS厂商的调用样例，填写了所有pos厂商需要填写的字段
    public void test_monitor_pos() {
        // POS厂商使用的交易信息格式，字符串类型
        List<PosTradeInfo> posTradeInfoList = new ArrayList<PosTradeInfo>();
        posTradeInfoList.add(PosTradeInfo.newInstance(HbStatus.S, "1324", 7));
        posTradeInfoList.add(PosTradeInfo.newInstance(HbStatus.X, "1326", 15));
        posTradeInfoList.add(PosTradeInfo.newInstance(HbStatus.S, "1401", 8));
        posTradeInfoList.add(PosTradeInfo.newInstance(HbStatus.F, "1405", 3));

        // 填写异常信息，如果有的话
        List<ExceptionInfo> exceptionInfoList = new ArrayList<ExceptionInfo>();
        exceptionInfoList.add(ExceptionInfo.HE_PRINTER);

        // 填写扩展参数，如果有的话
        Map<String, Object> extendInfo = new HashMap<String, Object>();
        //        extendInfo.put("SHOP_ID", "BJ_ZZ_001");
        //        extendInfo.put("TERMINAL_ID", "1234");

        AlipayHeartbeatSynRequestBuilder builder = new AlipayHeartbeatSynRequestBuilder()
                .setProduct(com.alipay.demo.trade.model.hb.Product.FP)
                .setType(Type.SOFT_POS)
                .setEquipmentId("soft100001")
                .setEquipmentStatus(EquipStatus.NORMAL)
                .setTime("2015-09-28 11:14:49")
                .setManufacturerPid("2088000000000009")
                // 填写机具商的支付宝pid
                .setStoreId("store200001").setEquipmentPosition("31.2433190000,121.5090750000")
                .setBbsPosition("2869719733-065|2896507033-091").setNetworkStatus("gggbbbgggnnn")
                .setNetworkType("3G").setBattery("98").setWifiMac("0a:00:27:00:00:00")
                .setWifiName("test_wifi_name").setIp("192.168.1.188")
                .setPosTradeInfoList(posTradeInfoList) // POS厂商同步trade_info信息
                //                .setExceptionInfoList(exceptionInfoList) // 填写异常信息，如果有的话
                .setExtendInfo(extendInfo) // 填写扩展信息，如果有的话
                ;

        MonitorHeartbeatSynResponse response = monitorService.heartbeatSyn(builder);
        dumpResponse(response);
    }

    // 测试当面付2.0支付
    public void test_trade_pay(AlipayTradeService service) {
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = "tradepay" + System.currentTimeMillis()
                + (long) (Math.random() * 10000000L);

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = "xxx品牌xxx门店当面付消费";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = "0.01";

        // (必填) 付款条码，用户支付宝钱包手机app点击“付款”产生的付款条码
        String authCode = "用户自己的支付宝付款码"; // 条码示例，286648048691290423
        // (可选，根据需要决定是否使用) 订单可打折金额，可以配合商家平台配置折扣活动，如果订单部分商品参与打折，可以将部分商品总价填写至此字段，默认全部商品可打折
        // 如果该值未传入,但传入了【订单总金额】,【不可打折金额】 则该值默认为【订单总金额】- 【不可打折金额】
        //        String discountableAmount = "1.00"; //

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0.0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = "购买商品3件共20.00元";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId(providerId);

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "5m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx面包", 1000, 1);
        // 创建好一个商品后添加至商品明细列表
        goodsDetailList.add(goods1);

        // 继续创建并添加第一条商品信息，用户购买的产品为“黑人牙刷”，单价为5.00元，购买了两件
        GoodsDetail goods2 = GoodsDetail.newInstance("goods_id002", "xxx牙刷", 500, 2);
        goodsDetailList.add(goods2);

        String appAuthToken = "应用授权令牌";//根据真实值填写

        // 创建条码支付请求builder，设置请求参数
        AlipayTradePayRequestBuilder builder = new AlipayTradePayRequestBuilder()
                //            .setAppAuthToken(appAuthToken)
                .setOutTradeNo(outTradeNo).setSubject(subject).setAuthCode(authCode)
                .setTotalAmount(totalAmount).setStoreId(storeId)
                .setUndiscountableAmount(undiscountableAmount).setBody(body).setOperatorId(operatorId)
                .setExtendParams(extendParams).setSellerId(sellerId)
                .setGoodsDetailList(goodsDetailList).setTimeoutExpress(timeoutExpress);

        // 调用tradePay方法获取当面付应答
        AlipayF2FPayResult result = service.tradePay(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝支付成功: )");
                break;

            case FAILED:
                log.error("支付宝支付失败!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，订单状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
    }

    // 测试当面付2.0查询订单
    public void test_trade_query() {
        // (必填) 商户订单号，通过此商户订单号查询当面付的交易状态
        String outTradeNo = "tradepay14817938139942440181";

        // 创建查询请求builder，设置请求参数
        AlipayTradeQueryRequestBuilder builder = new AlipayTradeQueryRequestBuilder()
                .setOutTradeNo(outTradeNo);

        AlipayF2FQueryResult result = tradeService.queryTradeResult(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("查询返回该订单支付成功: )");

                AlipayTradeQueryResponse response = result.getResponse();
                dumpResponse(response);

                log.info(response.getTradeStatus());
                if (Utils.isListNotEmpty(response.getFundBillList())) {
                    for (TradeFundBill bill : response.getFundBillList()) {
                        log.info(bill.getFundChannel() + ":" + bill.getAmount());
                    }
                }
                break;

            case FAILED:
                log.error("查询返回该订单支付失败或被关闭!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，订单支付状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
    }

    // 测试当面付2.0退款
    public void test_trade_refund() {
        // (必填) 外部订单号，需要退款交易的商户外部订单号
        String outTradeNo = "tradepay14817938139942440181";

        // (必填) 退款金额，该金额必须小于等于订单的支付金额，单位为元
        String refundAmount = "0.01";

        // (可选，需要支持重复退货时必填) 商户退款请求号，相同支付宝交易号下的不同退款请求号对应同一笔交易的不同退款申请，
        // 对于相同支付宝交易号下多笔相同商户退款请求号的退款交易，支付宝只会进行一次退款
        String outRequestNo = "";

        // (必填) 退款原因，可以说明用户退款原因，方便为商家后台提供统计
        String refundReason = "正常退款，用户买多了";

        // (必填) 商户门店编号，退款情况下可以为商家后台提供退款权限判定和统计等作用，详询支付宝技术支持
        String storeId = "test_store_id";

        // 创建退款请求builder，设置请求参数
        AlipayTradeRefundRequestBuilder builder = new AlipayTradeRefundRequestBuilder()
                .setOutTradeNo(outTradeNo).setRefundAmount(refundAmount).setRefundReason(refundReason)
                .setOutRequestNo(outRequestNo).setStoreId(storeId);

        AlipayF2FRefundResult result = tradeService.tradeRefund(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝退款成功: )");
                break;

            case FAILED:
                log.error("支付宝退款失败!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，订单退款状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
    }

    // 测试当面付2.0生成支付二维码
    public ServerResponse pay(Order order,String path) {
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo =String.valueOf(order.getOrderNo());

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = "订单号"+order.getOrderNo()+"消费"+order.getPayment().intValue()+"元";

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount =String.valueOf(order.getPayment().intValue());

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = "购买商品共"+order.getPayment().intValue()+"元";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList=orderItemMapper.selectAllByUseridAndOrderNo(order.getUserId(),order.getOrderNo());
        if(orderItemList!=null&&orderItemList.size()>0){
            for(OrderItem orderItem:orderItemList){
                // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
                GoodsDetail goods = GoodsDetail.newInstance(String.valueOf(orderItem.getProductId()), orderItem.getProductName(), orderItem.getCurrentUnitPrice().intValue(),
                        orderItem.getQuantity());
                // 创建好一个商品后添加至商品明细列表
                goodsDetailList.add(goods);
            }
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl("http://izt2kt.natappfree.cc/business/order/alipay_callback.do")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 需要修改为运行机器上的路径
                String filePath = String.format(path+"/qr-%s.png",
                        response.getOutTradeNo());
                log.info("filePath:" + filePath);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);

                File qrcodeFile=new File(filePath);
                try {
                    FTPUtils.uploadFile(Lists.newArrayList(qrcodeFile));
                    qrcodeFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Map<String,String> map= Maps.newHashMap();
                map.put("orderNo",String.valueOf(order.getOrderNo()));
                map.put("qrPath",PropertiesUtil.getProperty("imagehost")+"qr-"+response.getOutTradeNo()+".png");
                return ServerResponse.createBySuccess(map);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                break;

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                break;

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                break;
        }
        return ServerResponse.createByError("支付失败");
    }



}

