package br.com.paulohpfranco.aplicativodeestoque;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import br.com.paulohpfranco.aplicativodeestoque.data.StoreContract;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * TAG Padrão para o LOG
     **/
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * Campo TextView Nome do Produto
     **/
    TextView mProductName;

    /**
     * Campo TextView Preço do Produto
     **/
    TextView mProductPrice;

    /**
     * Campo TextView Quantity do Produto
     **/
    TextView mProductQuantity;

    /**
     * Campo TextView Nome do Fornecedor
     **/
    TextView mProductSupplierName;

    /**
     * Campo TextView Phone do Fornecedor
     **/
    TextView mProductSupplierPhone;

    /**
     * Button Mais
     **/
    Button mBtnPlus;

    /**
     * Button Menos
     **/
    Button mBtnLess;

    /**
     * Loader ID
     **/
    private static final int STORE_LOADER = 1;

    /**
     * Content URI para um produto existente (null se for um novo produto)
     */
    Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        mProductName = findViewById(R.id.tv_product_name);
        mProductPrice = findViewById(R.id.tv_product_price);
        mProductQuantity = findViewById(R.id.tv_product_quantity);
        mProductSupplierName = findViewById(R.id.tv_supplier_name);
        mProductSupplierPhone = findViewById(R.id.tv_supplier_phone);

        //Inicio o loader
        getSupportLoaderManager().initLoader(STORE_LOADER, null, this);

        //Botão adicionar quantidade
        mBtnPlus = findViewById(R.id.btnplus);
        mBtnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePressedQuantity(view);
            }
        });

        //Botão remover quantidade
        mBtnLess = findViewById(R.id.btnless);
        mBtnLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePressedQuantity(view);
            }
        });

        //Botão para realizar um pedido
        Button btnAddCart = findViewById(R.id.btn_add_cart);
        btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+mProductSupplierPhone.getText().toString().trim()));
                startActivity(intent);
            }
        });

    }

    private void changePressedQuantity(View button) {
        switch (button.getId()) {
            case R.id.btnplus:
                changeQuantity(true);
                break;
            case R.id.btnless:
                changeQuantity(false);
                break;
        }
    }

    private void changeQuantity(boolean action) {
        // Se ação for verdadeiro devo adicionar, senão eu removo

        ContentValues values = new ContentValues();

        int quantity = Integer.parseInt(mProductQuantity.getText().toString().trim());

        if(action) {
            // Se ação for verdadeiro devo adicionar, senão eu removo

            quantity++;

            values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        } else {

            if(quantity == 0) {
                Toast.makeText(this, R.string.details_quantity_failed_less, Toast.LENGTH_SHORT).show();
                return;
            }

            quantity--;
            values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        }

        int affectedRows = getContentResolver().update(mCurrentProductUri, values,null, null);

        if(affectedRows != 0) {
            Log.v(LOG_TAG, "Quantidade adicionada");
        } else {
            Log.v(LOG_TAG, "Não houve alteraçõas");
        }

    }

    /**
     * Realiza a deleção do produto no banco de dados
     */
    private void deleteProduct() {
        int rowsAffected = getContentResolver().delete(mCurrentProductUri, null, null);

        if(rowsAffected == 0) {
            Toast.makeText(this, R.string.editor_delete_product_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.editor_delete_product_successful, Toast.LENGTH_SHORT).show();
        }
        // Fecha a activity
        finish();
    }

    private void showDeleteConfirmationDialog() {
        // Cria um AlertDialog.Builder e configura a mensagem, e os listeners
        // para os botões positivo e negativo do dialogo.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // O usuário clicou no botão deletar, salvamos o produto
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // O usuário clicou no botão cancelar,
                // então fecha a caixa de dialogo e continua editando
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Cria e exibe o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.old options from the res/menu/menu_catalog.xml file.
        // This adds menu_catalog.xml items to the app bar.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_data:
                //Cria a Intent para {@link DetailsActivity}

                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);

                intent.setData(mCurrentProductUri);

                startActivity(intent);
                return true;
            case R.id.action_delete:
                //Evento do botão deletar
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                StoreContract.ProductEntry._ID,
                StoreContract.ProductEntry.COLUMN_PRODUCT_NAME,
                StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE};

        Log.v(LOG_TAG, "mCurrentProductUri: " + mCurrentProductUri);

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Acha as colunas de atributos product em que estamos interessados
            int productNameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productSupplierNameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int productSupplierPhoneColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            // Extrai o valor do Cursor para o índice de coluna dado
            String productName = cursor.getString(productNameColumnIndex);
            Double productPrice = cursor.getDouble(productPriceColumnIndex);
            int productQuantity = cursor.getInt(productQuantityColumnIndex);
            String productSupplierName = cursor.getString(productSupplierNameColumnIndex);
            String productSupplierPhone = cursor.getString(productSupplierPhoneColumnIndex);

            mProductName.setText(productName);
            mProductPrice.setText(Double.toString(productPrice));
            mProductQuantity.setText(Integer.toString(productQuantity));
            mProductSupplierName.setText(productSupplierName);
            mProductSupplierPhone.setText(productSupplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mProductPrice.setText(0);
        mProductQuantity.setText(0);
        mProductSupplierName.setText("");
        mProductSupplierPhone.setText("");
    }
}
