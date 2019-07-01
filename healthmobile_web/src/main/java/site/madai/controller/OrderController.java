package site.madai.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;
import site.madai.constant.MessageConstant;
import site.madai.constant.RedisMessageConstant;
import site.madai.entity.Result;
import site.madai.pojo.Order;
import site.madai.service.OrderService;
import site.madai.utils.SMSUtils;

import java.util.Map;

/**
 * @Project: site.madai.controller
 * @Author: ShaoDi Wang
 * @Date: Created in 2019-06-30 21:20
 * @Description:
 * @Version: 1.0
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @Autowired
    private JedisPool jedisPool;

    /**
     * 根据id查询预约信息，包括套餐信息和会员信息
     * @param id
     * @return
     */
    @RequestMapping("findById")
    public Result findById(Integer id){
        Result result =null;
        try{
            result = orderService.findById4Detail(id);
            //查询预约信息成功
            return new Result(true,MessageConstant.QUERY_ORDER_SUCCESS,result.getData());
        }catch (Exception e){
            e.printStackTrace();
            //查询预约信息失败
            return new Result(false,MessageConstant.QUERY_ORDER_FAIL);
        }
    }

    /**
     * 体检预约
     * @param map
     * @return
     */
    @RequestMapping("submit")
    public Result submitOrder(@RequestBody Map map){
        String telephone = (String) map.get("telephone");
        //从Redis中获取缓存的验证码，key为手机号+RedisConstant.SENDTYPE_ORDER
        String codeInRedis = jedisPool.getResource().get(
                telephone + RedisMessageConstant.SENDTYPE_ORDER);
        String validateCode = (String) map.get("validateCode");
        //校验手机验证码
        if(codeInRedis == null || !codeInRedis.equals(validateCode)){
            return new Result(false, MessageConstant.VALIDATECODE_ERROR);
        }
        Result result =null;
        //调用体检预约服务
        try{
            map.put("orderType", Order.ORDERTYPE_WEIXIN);
            result = orderService.order(map);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            //预约失败
            return result;
        }
//        if(result.isFlag()){
            //预约成功，发送短信通知
//            String orderDate = (String) map.get("orderDate");
//            try {
//                SMSUtils.sendShortMessage(SMSUtils.ORDER_NOTICE,telephone,orderDate);
//            } catch (ClientException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
