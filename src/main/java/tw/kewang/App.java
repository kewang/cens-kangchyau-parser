package tw.kewang;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class App {
    private static final boolean DEBUG = false;
    private static final String URLS[] = {"http://www.kangchyau.com.tw/tw/products_F01.html", "http://www.kangchyau.com.tw/tw/products_F02.html", "http://www.kangchyau.com.tw/en/products.html", "http://www.kangchyau.com.tw/en/products_F02.html"};
    private static final String LANGUAGES[] = {"tw", "tw", "en", "en"};
    private static final String WORDS[] = {"機型", "機型", "MODEL", "MODEL"};
    private static final String WORD2S[] = {"*網頁上之圖片及係數只備參考其尺寸，規格，樣式會因客戶人所訂購的配件不同而有所改變", "*網頁上之圖片及係數只備參考其尺寸，規格，樣式會因客戶人所訂購的配件不同而有所改變", "* Remark: The above machine specification and production range can be changed without any notice due to kind of different application.", "* Remark: The above machine specification and production range can be changed without any notice due to kind of different application."};

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        for (int k = 0; k < URLS.length; k++) {
            Document doc = Jsoup.connect(URLS[k]).get();

            Elements elements = doc.select(".products_i_list a");

            ArrayList<String> urls = new ArrayList<String>();

            for (Element element : elements) {
                urls.add("http://www.kangchyau.com.tw/" + LANGUAGES[k] + "/" + element.attr("href"));
            }

            ArrayList<Model> models = new ArrayList<Model>();

            // every product
            for (String url : urls) {
                Model model = new Model();

                model.url = url;

                doc = Jsoup.connect(url).get();

                elements = doc.select("#spec table tbody tr");

                for (int i = 0; i < elements.size(); i++) {
                    Element element = elements.get(i);

                    if (i == 0) {
                        Elements elementProductIds = element.select(".th2");

                        for (Element elementProductId : elementProductIds) {
                            model.productIds.add(elementProductId.text());
                        }
                    } else {
                        Elements elementItems = element.children();

                        Model.Item item = new Model.Item();

                        for (Element elementItem : elementItems) {
                            if (elementItem.className().equals("td_1")) {
                                item.key = elementItem.text().trim();
                            } else if (elementItem.className().equals("td_2")) {
                                item.values.add(elementItem.text().trim());
                                item.isTitle = false;
                            } else if (elementItem.className().equals("td_3")) {
                                item.key = elementItem.text().trim();
                                item.isTitle = true;
                            }
                        }

                        if (StringUtils.isEmpty(item.key) && CollectionUtils.isEmpty(item.values) && !item.isTitle) {
                            continue;
                        }

                        model.items.add(item);
                    }
                }

                if (DEBUG) {
                    System.out.println(model);
                }

                models.add(model);
            }

            for (Model model : models) {
                if (CollectionUtils.isEmpty(model.productIds) || CollectionUtils.isEmpty(model.items)) {
                    continue;
                }

                StringBuffer sb = new StringBuffer();

                sb.append("<table class=\"rwd-table\">\n")
                        .append("    <tr style=\"background-color: #009fb9;\">\n")
                        .append("        <th style=\"color: #fff;\">")
                        .append(WORDS[k])
                        .append("</th>\n");

                for (String productId : model.productIds) {
                    sb.append("        <th style=\"color: #fff;\">").append(productId).append("</th>\n");
                }

                sb.append("    </tr>\n").append("    <tr class=\"bigsize\">\n");

                for (String productId : model.productIds) {
                    sb.append("        <td data-th=\"")
                            .append(WORDS[k])
                            .append("\">").append(productId).append("</td>\n");
                }

                sb.append("    </tr>\n");

                boolean whiteColor = true;
                for (int i = 0; i < model.items.size(); i++) {
                    Model.Item item = model.items.get(i);

                    if (!item.isTitle) {
                        if (whiteColor) {
                            sb.append("    <tr>\n");
                        } else {
                            sb.append("    <tr style=\"background-color: #eee;\">\n");
                        }

                        sb.append("        <th>").append(item.key).append("</th>\n");

                        for (String value : item.values) {
                            sb.append("        <th>").append(value).append("</th>\n");
                        }

                        sb.append("    </tr>\n");

                        sb.append("    <tr class=\"bigsize\">\n");

                        for (int j = 0; j < model.productIds.size(); j++) {
                            sb.append("        <td data-th=\"");

                            String productId = model.productIds.get(j);

                            String value = item.values.get(j);

                            if (model.productIds.size() == 1) {
                                sb.append(item.key).append("\">").append(value).append("</td>\n");
                            } else {
                                sb.append(productId).append("/").append(item.key).append("\">").append(value).append("</td>\n");
                            }
                        }

                        sb.append("    </tr>\n");

                        whiteColor = !whiteColor;
                    } else {
                        sb.append("    <tr style=\"background-color: #ccc;\">\n")
                                .append("        <th style=\"color: #009fb9;\" colspan=\"")
                                .append(model.productIds.size() + 1).append("\">")
                                .append(item.key)
                                .append("</th>\n")
                                .append("    </tr>\n");

                        sb.append("    <tr class=\"bigsize\">\n")
                                .append("        <td class=\"colon\" data-th=\"")
                                .append(item.key)
                                .append("\"></td>\n")
                                .append("    </tr>\n");
                    }
                }

                sb.append("</table>\n");
                sb.append(WORD2S[k]);

                FileUtils.writeStringToFile(new File("out/" + model.productIds.toString() + "." + LANGUAGES[k] + ".text"), sb.toString(), Charset.defaultCharset());
            }
        }

        long end = System.currentTimeMillis();

        System.out.println("Times: " + (end - start) + "ms");
    }

    private static class Model {
        public String url;
        public ArrayList<String> productIds = new ArrayList<String>();
        public ArrayList<Item> items = new ArrayList<Item>();

        private static class Item {
            public String key;
            public ArrayList<String> values = new ArrayList<String>();
            public boolean isTitle;

            @Override
            public String toString() {
                return "Item{" +
                        "key='" + key + '\'' +
                        ", values=" + values +
                        ", isTitle=" + isTitle +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "Model{" +
                    "url='" + url + '\'' +
                    ", productIds=" + productIds +
                    ", items=" + items +
                    '}';
        }
    }
}
