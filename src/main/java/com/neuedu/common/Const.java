package com.neuedu.common;

public class Const {
    public  static  final  String  CURRENT_USER="current_user";
   public static final String USERNAME="username";
   public static final String EMAIL="email";

    public interface  USERROLE{
        public  int  ADMINUSER=0;//管理员
        public int  CUSTOMERUSER=1;//普通用户
    }

    public  enum  ProductStatusEnum{

        PRODUCT_ONLLINE(1,"在售"),
        PRODUCT_OFFLINE(2,"下架"),
        PRODUCT_DELETE(3,"删除")
        ;
        private int  status;
        private String desc;
        private  ProductStatusEnum(int status,String desc){
            this.status=status;
            this.desc=desc;
        }

        public int getStatus() {
            return status;
        }

        public String getDesc() {
            return desc;
        }
    }

    public  interface Cart{

        String LIMIT_NUM_SUCCESS="LIMIT_NUM_SUCESS";
        String  LIMIT_NUM_FAIL="LIMIT_NUM_FAIL";

        int CHECKED=1;//选中
        int UNCHECKED=0;//未选中

    }

    public  enum OrderStatusEnum{
     //0-已取消 10-未付款 20-已付款 40-已发货 50-交易成功 60-交易关闭
        ORDER_CANCELLED(0,"已取消"),
        ORDER_UN_PAY(10,"未付款"),
        ORDER_PAYED(20,"已付款"),
        ORDER_SEND(40,"已发货"),
        ORDER_SUCESS(50,"交易成功"),
        ORDER_CLOSED(60,"交易关闭"),
        ;
        private int  status;
        private String desc;
        private OrderStatusEnum(int status,String desc){
            this.status=status;
            this.desc=desc;
        }

        public  static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum:values()){
                if(orderStatusEnum.getStatus()==code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }

        public int getStatus() {
            return status;
        }

        public String getDesc() {
            return desc;
        }
    }
    public  enum PaymentTypeEnum{

        PAY_ON_LINE(1,"在线支付"),

        ;
        private int  status;
        private String desc;
        private PaymentTypeEnum(int status,String desc){
            this.status=status;
            this.desc=desc;
        }

        public  static PaymentTypeEnum codeOf(int code){
            for(PaymentTypeEnum paymentTypeEnum:values()){
              if(paymentTypeEnum.getStatus()==code){
                  return paymentTypeEnum;
              }
            }
           throw new RuntimeException("没有找到对应的枚举");
        }

        public int getStatus() {
            return status;
        }

        public String getDesc() {
            return desc;
        }
    }
}
