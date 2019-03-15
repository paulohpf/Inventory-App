package br.com.paulohpfranco.aplicativodeestoque;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import br.com.paulohpfranco.aplicativodeestoque.data.StoreContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * TAG Padrão para o LOG
     **/
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * Campo EditText Nome do Produto
     **/
    EditText mProductName;

    /**
     * Campo EditText Preço do Produto
     **/
    EditText mProductPrice;

    /**
     * Campo EditText Quantity do Produto
     **/
    EditText mProductQuantity;

    /**
     * Campo EditText Nome do Fornecedor
     **/
    EditText mProductSupplierName;

    /**
     * Campo EditText Phone do Fornecedor
     **/
    EditText mProductSupplierPhone;

    /**
     * Loader ID
     **/
    private static final int STORE_LOADER = 1;

    /**
     * Content URI para um produto existente (null se for um novo produto)
     */
    Uri mCurrentProductUri;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        /** Se o Intent não contiver o Uri de um produto, então ele está criando um novo **/
        if (mCurrentProductUri == null) {
            // Mudo o nome do Intent
            setTitle(getString(R.string.editor_activity_title_new_product));

            invalidateOptionsMenu();
        } else {
            // Mudo o nome do Intent
            setTitle(getString(R.string.editor_activity_title_update_product));

            //Inicio o loader
            getSupportLoaderManager().initLoader(STORE_LOADER, null, this);
        }

        mProductName = findViewById(R.id.edit_product_name);
        mProductPrice = findViewById(R.id.edit_product_price);
        mProductQuantity = findViewById(R.id.edit_product_quantity);
        mProductSupplierName = findViewById(R.id.edit_supplier_name);
        mProductSupplierPhone = findViewById(R.id.edit_supplier_phone);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductName.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);
        mProductSupplierName.setOnTouchListener(mTouchListener);
        mProductSupplierPhone.setOnTouchListener(mTouchListener);
    }

    /**
     * Capturo o input do usuário e salvo no banco de dados
     */
    private void saveProduct() {
        // Le os campos
        // O trim elimina os espaços apôs o texto
        // Extrai o valor do Cursor para o índice de coluna dado
        String productName = mProductName.getText().toString().trim();
        Double productPrice;
        int productQuantity;
        String productSupplierName = mProductSupplierName.getText().toString().trim();
        String productSupplierPhone = mProductSupplierPhone.getText().toString().trim();

        //Verifico se os campos estão vazios
        if (TextUtils.isEmpty(productName)
                || TextUtils.isEmpty(productSupplierName)
                || TextUtils.isEmpty(productSupplierPhone)) {
            Toast.makeText(this, R.string.editor_db_product_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        if(mProductPrice.getText().toString().trim().equals("")) {
            Toast.makeText(this, R.string.editor_db_product_failed, Toast.LENGTH_SHORT).show();
            return;
        } else {
            productPrice = Double.parseDouble(mProductPrice.getText().toString().trim());
        }

        if(mProductQuantity.getText().toString().trim().equals("")) {
            Toast.makeText(this, R.string.editor_db_product_failed, Toast.LENGTH_SHORT).show();
            return;
        } else {
            productQuantity = Integer.parseInt(mProductQuantity.getText().toString().trim());
        }

        // Cria um objeto ContentValues onde os nomes das colunas são chaves
        // e os atributos do produto são os valores
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, productSupplierName);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, productSupplierPhone);

        //Verifico se é uma adesão de um produto novo ou alteração
        if(mCurrentProductUri == null) {
            // Insere uma nova coluna de produto no banco de dados, retornando o ID dessa coluna
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Mostra um toast dependendo se a inserção ocorrer ou não
            if (newUri == null) {
                // Se a coluna é -1, então estamos com um erro na inserção
                Toast.makeText(this, R.string.editor_insert_product_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Senão, a inserção foi um sucesso
                Toast.makeText(this, R.string.editor_insert_product_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            Integer rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Mostra um toast dependendo se a inserção ocorrer ou não
            if (rowsUpdated == 0) {
                // Se não existem colunas afetadas, então estamos com um erro
                Toast.makeText(this, R.string.editor_update_product_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Senão, a atualização ocorreu com sucesso
                Toast.makeText(this, R.string.editor_update_product_successful, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Cria um AlertDialog.Builder e configura a mensagem e click listeners
        // para os botões positivos e negativos do dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // O usuário clicou no botão "Continuar editando", então, feche a caixa de diálogo
                // e continue editando.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Cria e mostra o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // Se o product não mudou, continue lidando com clique do botão "back"
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Caso contrário, se houver alterações não salvas, configure uma caixa de diálogo para alertar o usuário.
        // Crie um click listener para lidar com o usuário, confirmando que mudanças devem ser descartadas.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicou no botão "Discard", fecha a activity atual.
                        finish();
                    }
                };

        // Mostra o diálogo que diz que há mudanças não salvas
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Evento do botão salvar
                saveProduct();
                return true;
            case android.R.id.home:
                // Se o produto não mudou, continua navegando para cima, para a activity pai

                if (!mProductHasChanged) {
                    Intent intent = NavUtils.getParentActivityIntent(EditorActivity.this);
                    intent.setData(mCurrentProductUri);

                    NavUtils.navigateUpTo(this, intent);
                    return true;
                }

                // Caso contrário, se houver alterações não salvas, configura um diálogo para alertar o usuário.
                // Cria um click listener para lidar com o usuário, confirmando que
                // mudanças devem ser descartadas.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Usuário clidou no botão "Discard", e navegou para a activity pai.
                                Intent intent = NavUtils.getParentActivityIntent(EditorActivity.this);
                                intent.setData(mCurrentProductUri);

                                NavUtils.navigateUpTo(EditorActivity.this, intent);
                            }
                        };

                // Mostra um diálogo que notifica o usuário de que há alterações não salvas
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE};

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
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productSupplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int productSupplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

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
        mProductPrice.setText(Double.toString(0));
        mProductQuantity.setText(Integer.toString(0));
        mProductSupplierName.setText("");
        mProductSupplierPhone.setText("");
    }
}
