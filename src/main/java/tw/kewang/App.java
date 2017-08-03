package tw.kewang;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

        System.out.println(models);
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
