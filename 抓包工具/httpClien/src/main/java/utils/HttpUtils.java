package utils;

import java.awt.*;
import java.io.*;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * <p>类名: HttpUtils</p>
 * <p>描述: http请求工具类</p>
 * <p>修改时间: 2019年04月30日 上午10:12:35</p>
 *
 * @author lidongyang
 */
public class HttpUtils {

    public static String defaultEncoding = "utf-8";

    /**
     * 发送http post请求，并返回响应实体
     *
     * @param url 请求地址
     * @return url响应实体
     */
    public static String postRequest(String url) {
        return postRequest(url, null, (Map<String, Object>)null);
    }

    /**
     * <p>方法名: postRequest</p>
     * <p>描述: 发送httpPost请求</p>
     *
     * @param url
     * @param params
     * @return
     */
    public static String postRequest(String url, Map<String, Object> params) {
        return postRequest(url, null, params);
    }

    /**
     * 发送http post请求，并返回响应实体
     *
     * @param url     访问的url
     * @param headers 请求需要添加的请求头
     * @param params  请求参数
     * @return
     */
    public static String postRequest(String url, Map<String, String> headers,
                                     Map<String, Object> params) {
        String result = null;
        CloseableHttpClient httpClient = buildHttpClient();
        HttpPost httpPost = new HttpPost(url);

        if (null != headers && headers.size() > 0) {
            for (Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                httpPost.addHeader(new BasicHeader(key, value));
            }
        }
        if (null != params && params.size() > 0) {
            List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
            for (Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                pairList.add(pair);
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName(defaultEncoding)));
        }

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity,
                            Charset.forName(defaultEncoding));
                }
            } finally {
                response.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }


    public static String postRequest(String url, Map<String, String> headers,
                                     String body) {
        String result = null;
        CloseableHttpClient httpClient = buildHttpClient();
        HttpPost httpPost = new HttpPost(url);

        if (null != headers && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                httpPost.addHeader(new BasicHeader(key, value));
            }
        }


        httpPost.setEntity(new StringEntity(body, Charset.forName(defaultEncoding)));

        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    result = EntityUtils.toString(entity,
                            Charset.forName(defaultEncoding));
                }
            } finally {
                response.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 发送http get请求
     *
     * @param url 请求url
     * @return url返回内容
     */
    public static String getRequest(String url) {
        return getRequest(url, null);
    }


    /**
     * 发送http get请求
     *
     * @param url    请求的url
     * @param params 请求的参数
     * @return
     */
    public static String getRequest(String url, Map<String, Object> params) {
        return getRequest(url, null, params);
    }

    /**
     * 发送http get请求
     *
     * @param url        请求的url
     * @param headersMap 请求头
     * @param params     请求的参数
     * @return
     */
    public static String getRequest(String url, Map<String, String> headersMap, Map<String, Object> params) {
        String result = null;
        CloseableHttpClient httpClient = buildHttpClient();
        try {
            String apiUrl = url;
            if (null != params && params.size() > 0) {
                StringBuffer param = new StringBuffer();
                int i = 0;
                for (String key : params.keySet()) {
                    if (i == 0) {
                        param.append("?");
                    }else {
                        param.append("&");
                    }param.append(key).append("=").append(params.get(key));
                    i++;
                }
                apiUrl += param;
            }

            HttpGet httpGet = new HttpGet(apiUrl);
            if (null != headersMap && headersMap.size() > 0) {
                for (Entry<String, String> entry : headersMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    httpGet.addHeader(new BasicHeader(key, value));
                }
            }
            CloseableHttpResponse response = httpClient.execute(httpGet);
            try {
                HttpEntity entity = response.getEntity();
                if (null != entity) {
                    result = EntityUtils.toString(entity, defaultEncoding);
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 创建httpclient
     *
     * @return
     */
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


    private static String name = UUID.randomUUID().toString();
    static int downLoadCount = 0;

    /**
     * @author wangmuming
     * 此可以做完内部类 也可以不做未内部类
     * 作为内部类的时候 有一个好处 就是可以直接引用给类的主对象的成员变量 如此处的name
     * 当然
     */
    static class TestThread implements Runnable {
        private CountDownLatch threadsSignal;
        private int pageindex = 1;

        public TestThread(CountDownLatch threadsSignal, int pageindex) {
            this.threadsSignal = threadsSignal;
            this.pageindex = pageindex;
        }



        public void run() {
            System.out.println(Thread.currentThread().getName() + "开始..." + name);
            System.out.println("开始了线程：：：：" + threadsSignal.getCount());

            // do shomething
            // 设置页面参数
            Map<String, Object> paramsOne = new HashMap<String, Object>();
            // 请求的页面网址
            String url = "http://www.zycg.gov.cn/freecms/rest/v1/notice/selectInfoMoreChannel.do?&siteId=6f5243ee-d4d9-4b69-abbd-1e40576ccd7d&channel=d0e7c5f4-b93e-4478-b7fe-61110bb47fd5&currPage=" + pageindex + "&pageSize=1000&noticeType=2&implementWay=21&operationStartTime=&title=&operationEndTime=";
            // 拿到数据
            String result = HttpUtils.getRequest(url);
            // 转成json数据
            JSONObject jsonObject = JSON.parseObject(result);
            // 拿到data数组
            JSONArray array = jsonObject.getJSONArray("data");
            if (array.size() <= 0) {
                return;
            }

            // 设置详细的请求头
            Map<String, String> headersMap = new HashMap<String, String>();
            headersMap.put("Accept", "application/json, text/javascript, */*; q=0.01");
            headersMap.put("Accept-Encoding", "gzip, deflate");
            headersMap.put("Accept-Language", "zh-CN,zh;q=0.9");
            headersMap.put("Connection", "keep-alive");
            headersMap.put("Cookie", "HWWAFSESID=bcef07317bbe11ea1b; HWWAFSESTIME=1608187751029; Hm_lvt_8634e79bc7cdf26c2fc52ff6dbc9945f=1608187752; Hm_lpvt_8634e79bc7cdf26c2fc52ff6dbc9945f=1608187752");
            // headersMap.put("Postman-Token","<calculated when request is sent>");
            headersMap.put("Host", "mkt.zycg.gov.cn");
            headersMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
            headersMap.put("X-Requested-With", "XMLHttpRequest");

            // 空参 需要参数时传
            Map<String, Object> params = new HashMap<String, Object>();
            for (int i = 0; i < array.size(); i++) {
                try {
                    JSONObject o = array.getJSONObject(i);
                    // 继续下一层 拿到这主键
                    JSONObject fieldValues = JSON.parseObject(o.getString("fieldValues"));
                    // 拿到详情页中的url
                    String urlString = fieldValues.getString("f_noticeDetailUrl");
                    if (StringUtils.isEmpty(urlString)) {
                        continue;
                    }
                    // 拿到标识id
                    String noticeId = getId(urlString);
                    String detailurl = "http://mkt.zycg.gov.cn/proxy/platform/platform/notice/queryMallNoticeById?platformId=20&id=" + noticeId;
                    // 访问页面拿数据
                    String detailData = HttpUtils.getRequest(detailurl, headersMap, params);
                    // 转换json
                    JSONObject responseJson = JSON.parseObject(detailData);
                    JSONObject dataJson = JSON.parseObject(responseJson.getString("data"));
                    String contentStr = dataJson.getString("contentStr");

                    MyData myData = new MyData();
                    myData.setTime(dataJson.getString("publishTimeStr"));
                    myData.setName(StringUtils.substringBetween(contentStr, "<label>项目名称:</label> <span>", "</span>  </div>"));
                    myData.setAddr(StringUtils.substringBetween(contentStr, "<label id=\"deliveryAddress\">", "</label> </li>"));
                    myData.setPriceSuccess(StringUtils.substringBetween(contentStr, "<label>成交价:</label><span>", "</span></div>"));
                    myData.setUnit(StringUtils.substringBetween(contentStr, "<label>成交供应商:</label> <span>", "</span></div>"));
                    myData.setBudgeting(StringUtils.substringBetween(contentStr, "：</span><label id=\"projectBudget\"><span style=\"color:#D9001B;\">", "</span></label>"));
                    // 截取数量
                    String countData = StringUtils.substringBetween(contentStr, "供应商报价详情", "</table>");
                    Integer count = countInt(countData, "<tr>") - 1;
                    myData.setCount(count + "");
                    myDataList.add(myData);
                    downLoadCount++;
                    System.out.println("读取中央政府采购数据：" + downLoadCount + "次");
                } catch (Exception ex) {
                }
            }


            //核心处理逻辑

            //	用到成员变量name作为参数

            // 线程结束时计数器减1
            threadsSignal.countDown();//必须等核心处理逻辑处理完成后才可以减1
            System.out.println(Thread.currentThread().getName() + "结束. 还有"
                    + threadsSignal.getCount() + " 个线程");
        }
    }

    static Vector<MyData> myDataList = new Vector<MyData>();


    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException {
        // 1.初始化  countDown 线程数量
        int threadNum = 10;

        CountDownLatch threadSignal = new CountDownLatch(threadNum);
        // 创建固定长度的线程池
//		Executor executor = Executors.newFixedThreadPool(threadNum);
        //此处不可以用接口 需要使用Executor的实现类 ExecutorService  Executor未提供shutdown等方法
        // 2.创建10个线程池
        ExecutorService executor = Executors.newFixedThreadPool(threadNum);
        for (int i = 1; i <= threadNum; i++) { // 开threadNum个线程
            Runnable task = new TestThread(threadSignal, i);
            /* 执行 */
            executor.execute(task);
        }
        threadSignal.await(); // 等待所有子线程执行完
        //固定线程池执行完成后 将释放掉资源 退出主进程
        executor.shutdown();//并不是终止线程的运行，而是禁止在这个Executor中添加新的任务
        // do work end
        //退出主进程
        System.out.println(Thread.currentThread().getName() + "+++++++结束.");


//        String basePath = "/D:";
//        String fileName = "数据3.xls";

        // 导入excel
        HSSFWorkbook workbook = exportExcel(myDataList, MyData.class);
        workbook.setSheetName(0, "sheetName");//设置sheet的Name
        workbook.write(new File("D:/aaa.xls"));
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

    public static String getId(String str) {
        String[] id = str.split("=");
        return id[1];
    }

    public static Integer countInt(String str, String s) {
        int count = 0, len = str.length();
        while (str.indexOf(s) != -1) {
            str = str.substring(str.indexOf(s) + 1, str.length());
            count++;
        }
        return count;
    }

    public static void exportExcel(String sheetName, String[] headers, List<MyData> dataset, OutputStream out) {
        // 声明一个工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(sheetName);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth(15);
        // 生成一个样式
        HSSFCellStyle style = workbook.createCellStyle();
        // 设置这些样式
        //style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);

        // 生成一个字体
        HSSFFont font = workbook.createFont();
        //font.setColor(HSSFColor.VIOLET.index);
        // 把字体应用到当前的样式
        style.setFont(font);
        // 生成并设置另一个样式
        HSSFCellStyle style2 = workbook.createCellStyle();
        //style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        //style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        // 生成另一个字体
        HSSFFont font2 = workbook.createFont();

        // 把字体应用到当前的样式
        style2.setFont(font2);
        // 声明一个画图的顶级管理器
//        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        // 定义注释的大小和位置,详见文档
//        HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
        // 设置注释内容
//        comment.setString(new HSSFRichTextString("这是张勇波做的导出功能!"));
        // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.
//        comment.setAuthor("张勇波");
        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);
        }
        // 遍历集合数据，产生数据行
        Iterator<MyData> it = dataset.iterator();
        int index = 0;
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            MyData t = it.next();
            // 利用反射，根据javabean属性的先后顺序，动态调用getXxx()方法得到属性值
            Field[] fields = t.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                HSSFCell cell = row.createCell(i);
                cell.setCellStyle(style2);
                Field field = fields[i];
                String fieldName = field.getName();
                String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Class<?> tCls = t.getClass();
                Method getMethod = null;
                try {
                    getMethod = tCls.getMethod(getMethodName);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                Object value = null;
                try {
                    if (getMethod != null) {
                        value = getMethod.invoke(t);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                String val = "";
                if (value != null && !"null".equals(value) && !"".equals(value)) {
                    val = value.toString().trim();
                }
                if (0 < val.length() && val.length() < 9 && StringUtils.isNumeric(val)) {
                    // 是数字当作long处理
                    cell.setCellValue(Long.parseLong(val));
                } else {
                    HSSFRichTextString richString = new HSSFRichTextString(val);
//                    HSSFFont font3 = workbook.createFont();
                    //font3.setColor(HSSFColor.BLUE.index);
//                    richString.applyFont(font3);
                    cell.setCellValue(richString);
                }
            }
        }
        try {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}