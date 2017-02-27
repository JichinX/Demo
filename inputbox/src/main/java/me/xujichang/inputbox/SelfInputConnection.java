package me.xujichang.inputbox;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

/**
 * Created by Administrator on 2017/2/27.
 */

public class SelfInputConnection extends BaseInputConnection {
    public SelfInputConnection(View targetView, boolean fullEditor) {
        super(targetView, fullEditor);
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        return super.commitText(text, newCursorPosition);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if (beforeLength == 1 && afterLength == 0) {
            return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }
        return super.deleteSurroundingText(beforeLength, afterLength);
    }
}
