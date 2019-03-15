package br.com.paulohpfranco.aplicativodeestoque;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import br.com.paulohpfranco.aplicativodeestoque.data.StoreContract;
import br.com.paulohpfranco.aplicativodeestoque.data.StoreContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView tvName = view.findViewById(R.id.name);
        TextView tvSummary = view.findViewById(R.id.summary);
        TextView tvPrice = view.findViewById(R.id.price);
        Button btnSell = view.findViewById(R.id.btn_sell);

        Long id = cursor.getLong(cursor.getColumnIndexOrThrow(ProductEntry._ID));
        String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        final int productQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        Double productPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE));

        final Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        btnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productQuantity == 0) {
                    Toast.makeText(context, R.string.details_quantity_failed_less, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    ContentValues values = new ContentValues();
                    values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity-1);
                    context.getContentResolver().update(currentProductUri, values,null, null);
                }
            }
        });

        tvName.setText(productName);
        tvSummary.setText(Integer.toString(productQuantity));
        tvPrice.setText(Double.toString(productPrice));
    }
}
