package com.rks.musicx.ui.activities;

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.rks.musicx.MusicXApplication;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseActivity;
import com.rks.musicx.misc.utils.ColorGenerator;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.widgets.TextDrawable;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.Sku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/**
 * Created by Coolalien on 5/28/2017.
 */
public class DonationActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    /**
     * Product Id
     */

    private static String DONATION1 = "";
    private static String DONATION2 = "";
    private static String DONATION3 = "";
    private static String DONATION4 = "";
    private static String DONATION5 = "";
    private FastScrollRecyclerView rv;
    private ActivityCheckout mCheckout;
    private InventoryCallback mInventoryCallback;

    private static List<String> getInAppSkus() {
        final List<String> skus = new ArrayList<>();
        skus.addAll(Arrays.asList(DONATION1,DONATION2, DONATION3, DONATION4, DONATION5 ));
        for (int i = 0; i < 5; i++) {
            skus.add(String.valueOf(i));
        }
        return skus;
    }

    @Override
    protected int setLayout() {
        return R.layout.activity_donation;
    }

    @Override
    protected void setUi() {
        rv = (FastScrollRecyclerView) findViewById(R.id.commonrv);
    }

    @Override
    protected void function() {
        final Adapter adapter = new Adapter();
        mInventoryCallback = new InventoryCallback(adapter);
        String ateKey = Helper.getATEKey(this);
        int colorAccent = Config.accentColor(this, ateKey);
        rv.setPopupBgColor(colorAccent);
        rv.setItemAnimator(new DefaultItemAnimator());
        CustomLayoutManager customLayoutManager = new CustomLayoutManager(this);
        customLayoutManager.setSmoothScrollbarEnabled(true);
        rv.setLayoutManager(customLayoutManager);
        rv.addItemDecoration(new DividerItemDecoration(this, 75, false));
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);
        final Billing billing = MusicXApplication.get(this).getmBilling();
        mCheckout = Checkout.forActivity(this, billing);
        mCheckout.start();
        reloadInventory();
    }

    private void reloadInventory() {
        final Inventory.Request request = Inventory.Request.create();
        // load purchase info
        request.loadAllPurchases();
        // load SKU details
        request.loadSkus(ProductTypes.IN_APP, getInAppSkus());
        mCheckout.loadInventory(request, mInventoryCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCheckout.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mCheckout.stop();
        super.onDestroy();
    }

    private <T> RequestListener<T> makeRequestListener() {
        return new RequestListener<T>() {
            @Override
            public void onSuccess(@Nonnull T result) {
                reloadInventory();
            }

            @Override
            public void onError(int response, @Nonnull Exception e) {
                reloadInventory();
                e.printStackTrace();
            }
        };
    }

    private void consume(final Purchase purchase) {
        mCheckout.whenReady(new Checkout.EmptyListener() {
            @Override
            public void onReady(@Nonnull BillingRequests requests) {
                requests.consume(purchase.token, makeRequestListener());
            }
        });
    }

    private void purchase(Sku sku) {
        final RequestListener<Purchase> listener = makeRequestListener();
        mCheckout.startPurchaseFlow(sku, null, listener);
    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        return getStyleTheme();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private static class InventoryCallback implements Inventory.Callback {
        private final Adapter mAdapter;

        public InventoryCallback(Adapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onLoaded(@Nonnull Inventory.Products products) {
            final Inventory.Product product = products.get(ProductTypes.IN_APP);
            if (!product.supported) {
                // billing is not supported, user can't purchase anything
                return;
            }
            mAdapter.update(product);
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private final LayoutInflater mInflater = LayoutInflater.from(DonationActivity.this);
        private Inventory.Product mProduct = Inventory.Products.empty().get(ProductTypes.IN_APP);

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.donation_list, parent, false);
            return new ViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Sku sku = mProduct.getSkus().get(position);
            holder.onBind(sku, mProduct.isPurchased(sku));
        }

        @Override
        public int getItemCount() {
            return mProduct.getSkus().size();
        }

        public void update(Inventory.Product product) {
            mProduct = product;
            notifyDataSetChanged();
        }

        public void onClick(Sku sku) {
            final Purchase purchase = mProduct.getPurchaseInState(sku, Purchase.State.PURCHASED);
            if (purchase != null) {
                consume(purchase);
            } else {
                purchase(sku);
            }
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final Adapter mAdapter;
        TextView mTitle;
        TextView mDescription;
        TextView mPrice;
        ImageView mIcon;

        @Nullable
        private Sku mSku;

        private ViewHolder(View view, Adapter adapter) {
            super(view);
            mAdapter = adapter;
            mTitle = (TextView) view.findViewById(R.id.sku_title);
            mDescription = (TextView) view.findViewById(R.id.sku_description);
            mPrice = (TextView) view.findViewById(R.id.sku_price);
            mIcon = (ImageView) view.findViewById(R.id.sku_icon);
            view.setOnClickListener(this);
        }

        private void strikeThrough(TextView view, boolean strikeThrough) {
            int flags = view.getPaintFlags();
            if (strikeThrough) {
                flags |= Paint.STRIKE_THRU_TEXT_FLAG;
            } else {
                flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
            }
            view.setPaintFlags(flags);
        }

        private void onBind(Sku sku, boolean purchased) {
            mSku = sku;
            mTitle.setText(getTitle(sku));
            mDescription.setText(sku.description);
            strikeThrough(mTitle, purchased);
            strikeThrough(mDescription, purchased);
            mPrice.setText(sku.price);
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getRandomColor();
            String scaleTitle = getTitle(sku).substring(0,1);
            TextDrawable drawable = TextDrawable.builder().beginConfig()
                    .bold()
                    .toUpperCase()
                    .endConfig().buildRound(scaleTitle, color);
            mIcon.setImageDrawable(drawable);
            if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                mTitle.setTextColor(Color.WHITE);
                mDescription.setTextColor(ContextCompat.getColor(DonationActivity.this, R.color.darkthemeTextColor));
            } else {
                mTitle.setTextColor(Color.BLACK);
                mDescription.setTextColor(Color.DKGRAY);
            }
        }

        /**
         * @return SKU title without application name that is automatically added by Play Services
         */
        private String getTitle(Sku sku) {
            final int i = sku.title.indexOf("(");
            if (i > 0) {
                return sku.title.substring(0, i);
            }
            return sku.title;
        }

        @Override
        public void onClick(View v) {
            if (mSku == null) {
                return;
            }
            mAdapter.onClick(mSku);
        }
    }
}
