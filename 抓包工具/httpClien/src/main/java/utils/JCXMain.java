package utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.javafx.collections.MappingChange;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Row;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
@Author zd
@Date 2020/12/18 8:14
*/
public class JCXMain {
    public static String defaultEncoding = "utf-8";
    static Vector<MyData> myDataList = new Vector<MyData>();

    private static String name = UUID.randomUUID().toString();
    static int downLoadCount = 0;

    /**
     * @author wangmuming
     * 此可以做完内部类 也可以不做未内部类
     * 作为内部类的时候 有一个好处 就是可以直接引用给类的主对象的成员变量 如此处的name
     * 当然
     */
    private static class TestThread implements Runnable {
        private CountDownLatch threadsSignal;
        private int pageindex = 1;

        public TestThread(CountDownLatch threadsSignal, int pageindex) {
            this.threadsSignal = threadsSignal;
            this.pageindex = pageindex;
        }

        public void run() {
            // 设置第一个页面请求头
            Map<String, String> headersMap = new HashMap<String, String>();
            headersMap.put("Content-Type", "application/json;charset=UTF-8");
            headersMap.put("Host", "api.easyjcx.com");
            headersMap.put("sec-ch-ua", "\"Google Chrome\";v=\"87\", \" Not;A Brand\";v=\"99\", \"Chromium\";v=\"87\"");
            headersMap.put("sec-ch-ua-mobile", "?0");
            headersMap.put("Cookie", "HWWAFSESID=bcef07317bbe11ea1b; HWWAFSESTIME=1608187751029; Hm_lvt_8634e79bc7cdf26c2fc52ff6dbc9945f=1608187752; Hm_lpvt_8634e79bc7cdf26c2fc52ff6dbc9945f=1608187752");
            headersMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");

            // 设置页面参数
            Map<String, Object> paramsOne = new HashMap<String, Object>();

            // 访问的网址
            String url = "https://api.easyjcx.com/api/portalCli/bidResult";
            String s = "{\"keyword\":\"\",\"current\":" + pageindex + ",\"size\":100,\"condition\":{\"areaScope\":\"\",\"procurementType\":\"\",\"sourceType\":\"\",\"collegeNameList\":[],\"startBidresultTime\":\"\",\"endBidresultTime\":\"\"}}";

            // 获取结果
            String result = HttpUtils.postRequest(url, headersMap, s);

            // 空参
            Map<String, Object> params = new HashMap<String, Object>();


            // 拿到json数据
            JSONObject jsonObject = JSON.parseObject(result);
            // 拿到dataJSOn
            String dataStr = jsonObject.getString("data");
            JSONObject dataJsonAll = JSON.parseObject(dataStr);
            // 拿到data数组
            JSONArray array = dataJsonAll.getJSONArray("records");
            if (array.size() <= 0) {
                return;
            }

            for (int i = 0; i < array.size(); i++) {
                try {
                    JSONObject o = array.getJSONObject(i);

                    // 继续下一层
                    String orderMainId = o.getString("orderMainId");
                    if (StringUtils.isEmpty(orderMainId)) {
                        continue;
                    }
                    // 拼接地址
                    String detailurl = "https://api.easyjcx.com/api/portalCli/bidResult/detail";
                    //String bodyDetail="{\"orderMainId\":\"378913afaec94ff48153bf61d686b3d6\"}";
                    // 拼接地址id
                    String bodyDetail = "{\"orderMainId\":\"" + orderMainId + "\"}";
                    // 访问页面拿数据
                    String detailData = HttpUtils.postRequest(detailurl, headersMap, bodyDetail);
                    // 转换json
                    JSONObject responseJson11 = JSON.parseObject(detailData);
                    // 拿到datajosn
                    JSONObject responseJson = JSON.parseObject(responseJson11.getString("data"));

                    MyData myData = new MyData();
                    myData.setTime(responseJson.getString("publishBidresultTime"));
                    myData.setName(responseJson.getString("orderTitle"));
                    myData.setAddr("地址无");
                    myData.setUnit(responseJson.getString("collegeName"));
                    myData.setPriceSuccess(responseJson.getString("bidAmount"));
                    myData.setBudgeting("预算无");
                    myData.setCount("数量无");
                    myDataList.add(myData);
                    downLoadCount++;
                    System.out.println("读取竞彩星数据：" + downLoadCount + "次");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            // 线程结束时计数器减1
            threadsSignal.countDown();//必须等核心处理逻辑处理完成后才可以减1
            System.out.println(Thread.currentThread().getName() + "结束. 还有"
                    + threadsSignal.getCount() + " 个线程");

        }
    }


    //
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException {
        //List<MyData> myDataList=new ArrayList<MyData>();
        int threadNum = 270;
        // 初始化countDown
        CountDownLatch threadSignal = new CountDownLatch(threadNum);
        // 创建固定长度的线程池
//		Executor executor = Executors.newFixedThreadPool(threadNum);
        //此处不可以用接口 需要使用Executor的实现类 ExecutorService  Executor未提供shutdown等方法
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        for (int i = 1; i <= threadNum; i++) { // 开threadNum个线程
            Runnable task = new JCXMain.TestThread(threadSignal, i);
            /* 执行 */
            executor.execute(task);
        }
        threadSignal.await(); // 等待所有子线程执行完
        //固定线程池执行完成后 将释放掉资源 退出主进程
        executor.shutdown();//并不是终止线程的运行，而是禁止在这个Executor中添加新的任务
        // do work end
        //退出主进程
        System.out.println(Thread.currentThread().getName() + "+++++++结束.");
        String basePath = "/D:";
        String fileName = "竞彩星数据.xls";
        HSSFWorkbook workbook = exportExcel(myDataList, MyData.class);
        workbook.setSheetName(0, "sheetName");//设置sheet的Name
        workbook.write(new File(basePath + File.separator + fileName));
        System.out.println("完成。。。。。。。。。。。。");

    }


    /**
     * @param data 需要导出的数据
     * @param clz  数据对应的实体类
     * @param <T>  泛型
     * @return Excel文件
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static <T> HSSFWorkbook exportExcel(List<MyData> data, Class<T> clz) throws NoSuchFieldException, IllegalAccessException {

        Field[] fields = clz.getDeclaredFields();
        List<String> headers = new ArrayList<String>();
        List<String> variables = new ArrayList<String>();

        // 创建工作薄对象
        HSSFWorkbook workbook = new HSSFWorkbook();//这里也可以设置sheet的Name
        // 创建工作表对象
        HSSFSheet sheet = workbook.createSheet();
        // 创建表头
        Row rowHeader = sheet.createRow(0);

        // 表头处理
        for (int h = 0; h < fields.length; h++) {
            Field field = fields[h];
            if (field.isAnnotationPresent(ExcelHeader.class)) {
                // 表头
                ExcelHeader annotation = field.getAnnotation(ExcelHeader.class);
                headers.add(annotation.value());
                rowHeader.createCell(h).setCellValue(annotation.value());

                // 字段
                variables.add(field.getName());
            }
        }

        // 数据处理
        for (int i = 0; i < data.size(); i++) {

            //创建工作表的行(表头占用1行, 这里从第二行开始)
            HSSFRow row = sheet.createRow(i + 1);
            // 获取一行数据
            MyData t = (MyData) data.get(i);
            Class<?> aClass = t.getClass();
            // 填充列数据
            for (int j = 0; j < variables.size(); j++) {

                Field declaredField = aClass.getDeclaredField(variables.get(j));
                declaredField.setAccessible(true);

                String key = declaredField.getName();
                Object value = declaredField.get(t);

                row.createCell(j).setCellValue(value.toString());

            }
        }
        return workbook;
    }


    public static CloseableHttpClient buildHttpClient() {
        try {
            RegistryBuilder<ConnectionSocketFactory> builder = RegistryBuilder
                    .create();
            ConnectionSocketFactory factory = new PlainConnectionSocketFactory();
            builder.register("http", factory);
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            SSLContext context = SSLContexts.custom().useTLS()
                    .loadTrustMaterial(trustStore, new TrustStrategy() {
                        public boolean isTrusted(X509Certificate[] chain,
                                                 String authType) throws CertificateException {
                            return true;
                        }
                    }).build();
            LayeredConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(
                    context,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            builder.register("https", sslFactory);
            Registry<ConnectionSocketFactory> registry = builder.build();
            PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(
                    registry);
            ConnectionConfig connConfig = ConnectionConfig.custom()
                    .setCharset(Charset.forName(defaultEncoding)).build();
            SocketConfig socketConfig = SocketConfig.custom()
                    .setSoTimeout(100000).build();
            manager.setDefaultConnectionConfig(connConfig);
            manager.setDefaultSocketConfig(socketConfig);
            return HttpClientBuilder.create().setConnectionManager(manager)
                    .build();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


}
