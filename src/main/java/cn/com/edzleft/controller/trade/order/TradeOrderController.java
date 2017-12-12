package cn.com.edzleft.controller.trade.order;

import cn.com.edzleft.entity.*;
import cn.com.edzleft.service.trade.freight.FreightService;
import cn.com.edzleft.service.trade.order.TradeOrderService;
import cn.com.edzleft.service.trade.receivingAddress.ReceivingAddressService;
import cn.com.edzleft.service.trade.reject.RejectService;
import cn.com.edzleft.util.page.DataGridJson;
import cn.com.edzleft.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * Created by ibmtech on 2017/11/28.
 */
@Controller
@RequestMapping("/tradeOrder")
public class TradeOrderController {

    @Autowired
    private TradeOrderService tradeOrderService;

    @Autowired
    private FreightService freightService;

    @Autowired
    private ReceivingAddressService receivingAddressService;

    @Autowired
    private RejectService rejectService;
    /**
     * 订单查询
     * @param pageNumber
     * @param pageSize
     * @param orderStatus
     * @param orderCreatTime
     * @param orderCreator
     * @param principalOrder
     * @return
     */
    @RequestMapping(value = "/orderSelect" ,method = RequestMethod.POST)
    @ResponseBody
    public DataGridJson queryByPage(Integer pageNumber, Integer pageSize , String orderStatus, String orderCreatTime, String orderCreator, String principalOrder, HttpSession session){
        PageUtil<Order> pageUtil = new PageUtil<Order>();
        HashMap<String,Object> whereMaps =new HashMap<>();

        whereMaps.put("orderStatus",orderStatus);
        whereMaps.put("orderCreatTime",orderCreatTime);
        whereMaps.put("orderCreator",orderCreator);
        whereMaps.put("principalOrder",principalOrder);
        SessionInfo sessionInfo = (SessionInfo) session.getAttribute("sessionInfo");
        Account sessionAccount=sessionInfo.getAdmin();
        Integer informationId = sessionAccount.getInformationId();

        whereMaps.put("informationId",informationId);
        DataGridJson dgj = new DataGridJson();
        pageUtil.setCpage(pageNumber);
        pageUtil.setPageSize(pageSize);
        pageUtil.setWhereMap(whereMaps);

        pageUtil=tradeOrderService.queryAllOrder(pageUtil);
        dgj.setTotal(pageUtil.getTotalCount());
        dgj.setRows(pageUtil.getList());
        return dgj;
    }

    /**
     * 根据编号查询订单详情
     *
     */
    @RequestMapping(value = "/getOrderById",method = RequestMethod.POST)
    @ResponseBody
    public DataGridJson orderDetail(String orderNumber){
        DataGridJson dgj = new DataGridJson();
        HashMap<Object, Object> hashMap = new HashMap<>();
        Order order = tradeOrderService.queryOrderByNumber(orderNumber);

        //根据订单中货运单号获取货运id
        Integer freightNumberId = order.getFreightNumberId();
        //根据id获取货运单位信息
        Freight freight = freightService.queryFreightById(freightNumberId);

        //根据订单中的收货地址获取收货地址id
        Integer addressId = order.getReceivingAddressId();
        //根据收货地址id获取收货信息
        ReceivingAddress receivingAddress =receivingAddressService.queryReceivingAddress(addressId);

        //将查询到的信息存到map集合中
        hashMap.put("order",order);
        hashMap.put("freight",freight);
        hashMap.put("receivingAddress",receivingAddress);
        dgj.setHashMap(hashMap);
        return dgj;
    }





    /**
     * 领取订单
     *
     */
    @RequestMapping(value = "/lqdd",method = RequestMethod.POST)
    @ResponseBody
    public Order lqdd(Integer id,Integer flag, HttpSession session){
        Order order = tradeOrderService.setOrderStatus(id,flag,session);
        return order;
    }


    /**
     *
     * 驳回
     */
    @RequestMapping(value = "/bohui",method = RequestMethod.POST)
    @ResponseBody
    public Order rejectReason(Integer id,Integer flag,HttpSession session){
        Order order = tradeOrderService.setOrderStatus(id,flag, session);
        return order;
    }




    /**
     * 配置发货表单提交 成功后（修改订单状态）
     */
    @RequestMapping(value = "/pzfh",method = RequestMethod.POST)
    @ResponseBody
    public List<Freight> takeOrder(Integer id, Integer flag,Integer freightNumber,String freightUnit,String invoiceNum,HttpSession session) throws IOException {

//        //1.接受上传图片 ,保存到服务器上(使用uuid处理文件名)
//        File f=new File("D:\\tomcat_7.0.75\\apache-tomcat-7.0.75\\webapps\\newFile");
//        file.transferTo(f);
//        //获取文件原始名字
//        String str=file.getOriginalFilename();
//        //文件名字唯一
//        String imageName=UUID.randomUUID().toString()+FilenameUtils.getExtension(str);
//        //2.将上传的图片信息保存到数据库


//        //1.接受上传图片 ,保存到服务器上(使用uuid处理文件名)
//        File f=new File("D:\\tomcat_7.0.75\\apache-tomcat-7.0.75\\webapps\\newFile");
//        //获取图片名字
//        String originalFilename=file.getOriginalFilename();
//        //将图片名字唯一
//       String uniqueFilename=UUID.randomUUID()+"."+ FilenameUtils.getExtension(originalFilename);
//       //动态获取服务器路径
//        String realPath=session.getServletContext().getRealPath("/d");
//        //获得文件文件后缀
//        String subfix = FilenameUtils.getExtension(uniqueFilename);
//        //采用时间戳的方式将图片文件名唯一化
//        String prefix = new SimpleDateFormat("yyyyMMddhhssSSS").format(new Date());
//        //拼接图片文件全名
//        String fullName=prefix+"."+subfix;
//        file.transferTo(new File(realPath+File.separator+uniqueFilename));


        //根据id获取当前的订单对象
        Order order = tradeOrderService.queryOrderById(id);
        //根据id查询对应订单的货运管理
        Integer freightNumberId = order.getFreightNumberId();
        List<Freight> freightList = freightService.queryFreightByOrder(freightNumberId);
        //设置货运编号
        order.setFreightNumberId(freightNumber);
        //设置货运单位
        order.setFreightUnit(freightUnit);
        //设置发票号
        order.setInvoiceNum(invoiceNum);
        //调用业务配置发货
        tradeOrderService.updateOrder(order);
        //设置订单状态
        tradeOrderService.setOrderStatus(id,flag,session);
        System.out.println("表单提交成功");
        System.out.println("设置订单状态结束");
        return freightList;
    }

}
