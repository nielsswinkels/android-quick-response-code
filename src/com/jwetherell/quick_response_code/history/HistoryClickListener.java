/*
 * Copyright (C) 2009 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jwetherell.quick_response_code.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;

import java.util.List;

import com.jwetherell.quick_response_code.IDecoderActivity;
import com.jwetherell.quick_response_code.R;
import com.jwetherell.quick_response_code.core.Result;


final class HistoryClickListener implements DialogInterface.OnClickListener {

    private final HistoryManager historyManager;
    private final Activity activity;
    private final List<Result> items;

    /**
     * Handles clicks in the History dialog.
     * 
     * @author dswitkin@google.com (Daniel Switkin)
     * @author Sean Owen
     */
    HistoryClickListener(HistoryManager historyManager, Activity activity, List<Result> items) {
        this.historyManager = historyManager;
        this.activity = activity;
        this.items = items;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == items.size()) {
            // Share history.
            CharSequence history = historyManager.buildHistory();
            Uri historyFile = HistoryManager.saveHistory(history.toString());
            if (historyFile == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(R.string.msg_unmount_usb);
                builder.setPositiveButton(R.string.button_ok, null);
                builder.show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            String subject = activity.getResources().getString(R.string.history_email_title);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, subject);
            intent.putExtra(Intent.EXTRA_STREAM, historyFile);
            intent.setType("text/csv");
            activity.startActivity(intent);
        } else if (i == items.size() + 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.msg_sure);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface2, int i2) {
                    historyManager.clearHistory();
                }
            });
            builder.setNegativeButton(R.string.button_cancel, null);
            builder.show();
        } else if (activity instanceof IDecoderActivity) {
            // Display a single history entry.
            Result result = items.get(i);
            Message message = Message.obtain(((IDecoderActivity) activity).getHandler(),
                    R.id.decode_succeeded, result);
            message.sendToTarget();
        }
    }

}
