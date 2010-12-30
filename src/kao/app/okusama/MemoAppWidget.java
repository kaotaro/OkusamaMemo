package kao.app.okusama;

import org.kao.okusama.R;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

public class MemoAppWidget extends AppWidgetProvider{
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] addWidgetIds){
		updateAppWidget(context);
	}
	
	public static void updateAppWidget(Context context){
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.memo_widget_layout);
		views.setTextViewText(R.id.memo_widget, "hoge,  fuga,  hoo");
		manager.updateAppWidget(new ComponentName(context,MemoAppWidget.class), views);
	}
}
