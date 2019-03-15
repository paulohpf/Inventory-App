package br.com.paulohpfranco.aplicativodeestoque.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import br.com.paulohpfranco.aplicativodeestoque.data.StoreContract.ProductEntry;

public class StoreProvider extends ContentProvider {

    /** Database helper object **/
    private StoreDbHelper mDbHelper;

    /** Tag for the log messages **/
    public static final String LOG_TAG = StoreProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the store table */
    private static final int STORE = 100;

    /** URI matcher code for the content URI for a single product in the store table */
    private static final int STORE_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_STORE, STORE);
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_STORE + "/#", STORE_ID);
        Log.v(LOG_TAG, "StoreProvider iniciado");
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case STORE:
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case STORE_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
                default:
                    throw new IllegalArgumentException("Cannot query unknown Uri " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STORE:
                return ProductEntry.CONTENT_LIST_TYPE;
            case STORE_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STORE:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        String productName = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if(productName == null) {
            throw new IllegalArgumentException("Product requires a Name");
        }

        String supplierName = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if(supplierName == null) {
            throw new IllegalArgumentException("Product requires a Supplier Name");
        }

        String supplierPhone = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if(supplierPhone == null) {
            throw new IllegalArgumentException("Product requires a Supplier Phone");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long newRowId = db.insert(ProductEntry.TABLE_NAME, null, values);

        // Se o ID é -1, então a inserção falhou. Logue um error e retorne nulo.
        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        Log.v(LOG_TAG, "URI: "+ uri);

        return Uri.withAppendedPath(uri, "/" + newRowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Obtém banco de dados com permissão de escrita
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STORE:
                // Deleta todos os registros que correspondem a selection e selection args

                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STORE_ID:
                // Deleta um único registro dado pelo ID na URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Se 1 ou mais registros foram deletados, então notifica todos os listeners que os dados do
        // dado URI mudaram
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Retorna o número de registros deletados
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STORE:
                return updateProduct(uri, values, selection, selectionArgs);
            case STORE_ID:
                // Para o código STORE_ID, extraia o ID do URI,
                // para que saibamos qual registro atualizar. Selection será "_id=?" and selection
                // args será um String array contendo o atual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Se a chave {@link ProductEntry#COLUMN_PRODUCT_NAME} está presente,
        // checa se o valor do nome não é nulo.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // Se a chave {@link ProductEntry#COLUMN_PRODUCT_SUPPLIER_NAME} está presente,
        // checa se o valor do nome do vendedor não é nulo.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a Supplier Name");
            }
        }

        // Se a chave {@link ProductEntry#COLUMN_PRODUCT_SUPPLIER_PHONE} está presente,
        // checa se o valor do telefone do vendedor não é nulo.
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a Supplier Phone");
            }
        }

        // Se não há valores parar atualizar, então não tenta atualizar o banco de dados
        if (values.size() == 0) {
            return 0;
        }

        // Caso contrário, obtém o banco de dados com permissão de escrita e para atualizar os dados
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Executa a atualização no banco de dados e obtém o número de linhas afetadas
        int rowsUpdated = db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // Se 1 ou mais linhas foram atualizadas, então notifica todos os listeners que os dados na
        // dada URI mudaram
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Retorna o número de registros do banco de dados afetados pelo comando update
        return rowsUpdated;
    }
}
