package br.com.paulohpfranco.aplicativodeestoque.data;

import android.provider.BaseColumns;

public class StoreContract {

    // Prevent errors
    private StoreContract() {}

    /**
     * Inner class that defines constant values for the store database table.
     * Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

        public final static String TABLE_NAME = "product";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "product_name";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";
        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE = "supplier_phone";

    }

}
