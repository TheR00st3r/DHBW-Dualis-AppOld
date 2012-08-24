package dhbw.stundenplan.ui.element;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class TableCell extends TextView
{
	public TableCell(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public TableCell(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public TableCell(Context context)
	{
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		Rect rect = new Rect();
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(3);
		getDrawingRect(rect);
		canvas.drawRect(rect, paint);
	}
}
