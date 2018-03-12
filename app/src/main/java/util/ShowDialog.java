package util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import sure.gomotest.R;


/**
 * Created by dell88 on 2018/1/23 0023.
 */

public class ShowDialog {
    public static void showImageDialog(Context context,String shopphone){
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_show_qr, null);
        Dialog bottomDialog = new Dialog(context, R.style.BottomDialog);
        final ImageView imageView = (ImageView) contentView.findViewById(R.id.QR_image);
        Bitmap mBitmap = QRCodeUtil.createQRCodeBitmap(shopphone, 480, 480);
        imageView.setImageBitmap(mBitmap);
        bottomDialog.setContentView(contentView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        params.width = context.getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(context, 16f);
        params.bottomMargin = DensityUtil.dp2px(context, 8f);
        contentView.setLayoutParams(params);
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.getWindow().setGravity(Gravity.CENTER);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }
}
