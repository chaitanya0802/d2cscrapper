package brandpages;

import java.util.ArrayList;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CategoryToInt {
    static Properties cat_prop;

    // convert category names to list of their ids
    public static ArrayList<Integer> getListOfCat(String subcat){
        //load props
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/category_ids.properties");
            cat_prop = new Properties();
            cat_prop.load(fis);

        } catch (FileNotFoundException exc) {
            System.out.println(exc.toString());
        } catch (IOException ioex) {
            System.out.println(ioex.toString());
        }

        ArrayList<Integer> cate_ids = new ArrayList<>();

        try {

            // subcat
            String[] subcat_arr = subcat.split("\\+"); // Escape '+' for regex
            for (String s : subcat_arr) {
                String subCatValue = cat_prop.getProperty(s);
                if (subCatValue == null && s.length() > 2) {
                    System.out.println("@@@@ Property not found for subcategory: " + s);
                } else {
                    cate_ids.add(Integer.parseInt(subCatValue));
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }

        return cate_ids;

    }
    
}
