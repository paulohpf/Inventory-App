package br.com.paulohpfranco.aplicativodeestoque;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import br.com.paulohpfranco.aplicativodeestoque.data.StoreContract.ProductEntry;
import br.com.paulohpfranco.aplicativodeestoque.data.StoreDbHelper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** TAG Padrão para o LOG**/
    public static final String LOG_TAG = StoreDbHelper.class.getSimpleName();

    /** Loader ID **/
    private static final int STORE_LOADER = 1;

    /** Cursor Adapter de produto**/
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView mListView = findViewById(R.id.list_view_store);
        View emptyView = findViewById(R.id.empty_view);

        mListView.setEmptyView(emptyView);

        mCursorAdapter = new ProductCursorAdapter(this,null);
        mListView.setAdapter(mCursorAdapter);

        getSupportLoaderManager().initLoader(STORE_LOADER, null, this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Cria a Intent para {@link DetailsActivity}

                Log.v(LOG_TAG, "Click");

                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);

                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                intent.setData(currentProductUri);

                startActivity(intent);
            }
        });
    }

    private void insertProduct() {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Some book");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 0);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "Unknown person");
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, "+55143332211");

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        Log.v(LOG_TAG, "new Uri" + newUri);
    }

    private void deleteProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);

        if(rowsDeleted == 0) {
            Toast.makeText(this, R.string.action_delete_all_data_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.action_delete_all_data_success, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.old options from the res/menu/menu_catalog.xml file.
        // This adds menu_catalog.xml items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu.old option in the app bar overflow menu.old
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu.old option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            case R.id.action_delete_all_data:
                deleteProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
