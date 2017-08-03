package tw.kewang;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("http://www.kangchyau.com.tw/en/products.html").get();

        Elements elements = doc.select(".products_i_list a");

        ArrayList<String> urls = new ArrayList<String>();

        for (Element element : elements) {
            urls.add("http://www.kangchyau.com.tw/en/" + element.attr("href"));
        }

        ArrayList<Model> models = new ArrayList<Model>();

        // every product
        for (String url : urls) {
            Model model = new Model();

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
                            item.key = elementItem.text();
                        } else if (elementItem.className().equals("td_2")) {
                            item.values.add(elementItem.text());
                            item.isTitle = false;
                        } else if (elementItem.className().equals("td_3")) {
                            item.key = elementItem.text();
                            item.values = null;
                            item.isTitle = true;
                        }
                    }

                    model.items.add(item);
                }
            }

            models.add(model);
        }

        for (Model model : models) {
            StringBuffer sb = new StringBuffer();

            sb.append("<table class=\"rwd-table\">\n")
                    .append("    <tr style=\"background-color: #009fb9;\">\n")
                    .append("        <th style=\"color: #fff;\">MODEL</th>\n");

            for (String productId : model.productIds) {
                sb.append("<th style=\"color: #fff;\">").append(productId).append("</th>\n");
            }

            sb.append("</tr>\n").append("    <tr class=\"bigsize\">\n");

            for (String productId : model.productIds) {
                sb.append("        <td data-th=\"MODEL\">").append(productId).append("</td>\n");
            }

            sb.append("</tr>\n");

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

                    sb.append("    <tr class=\"bigsize\">").append("        <td data-th=\"");

                    for (int j = 0; j < model.productIds.size(); j++) {
                        String productId = model.productIds.get(j);
                        String value = item.values.get(j);

                        sb.append(productId).append("/").append(item.key).append("\">").append(value).append("</td>\n");
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
                }
            }

            sb.append("</table>\n");
            sb.append("* Remark: The above machine specification and production range can be changed without any notice due to kind of different application.");

            FileUtils.writeStringToFile(new File(model.productIds.toString() + ".text"), sb.toString(), Charset.defaultCharset());
        }
    }

    private static class Model {
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
                    "productIds=" + productIds +
                    ", items=" + items +
                    '}';
        }
    }
}
