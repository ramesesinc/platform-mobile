/*
 * MsgDialog.java
 *
 * Created on January 31, 2014, 9:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.rameses.client.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 *
 * @author wflores 
 */
public class UIDialog
{
    private final static Object LOCKED = new Object();

    public static void showMessage(Object message) {
        showMessage(message, null); 
    }
    
    public static void showMessage(Object message, Context context) {
        new UIDialog(context).alert(message);
    }

    public static void showError(Throwable error) {
        showError(error, null); 
    }
    
    public static void showError(Throwable error, Context context) {
        new UIDialog(context).alert(error);
    }
    
    
    private Context context;
    
    public UIDialog() {
        this(null); 
    }
    
    public UIDialog(Context context) {
        this.context = context; 
    }
    
    public Context getContext() {
        Context current = context;
        if (context == null) {
            context = Platform.getCurrentActivity();
        } 
        if (context == null) { 
            Exception e = new Exception("Context is not set");
            e.printStackTrace();
        }
        return context;
    }

    public void onCancel() {}     
    public void onApprove() {} 
    public void onApprove(Object value) {} 
    public void onSelectItem(int index) {}

    public final void alert(Throwable error) {
        new AlertBox().show(this, error);
    }    
    public final void alert(Object message) {
        new AlertBox().show(this, message);
    }

    public final void confirm(Object message) { 
        new ConfirmationBox().show(this, message); 
    } 
    
    public final void select(Object[] items) {
        new SelectionBox().show(this, items);
    }      
    
    public final void input(Object value) {
        input(value, "Input");
    } 
    
    public final void input(Object value, String title) {
        new InputBox().show(this, value, title);
    } 
    
    
    
    private static class AlertBox 
    {
        void show(final UIDialog caller, Throwable error) {
            Context context = caller.getContext();
            if (context == null) return; 
            
            DialogInterface.OnClickListener approvelistener = new DialogInterface.OnClickListener() {	
                public void onClick(DialogInterface dialog, int which) {
                    caller.onApprove();
                }
            }; 

            byte[] bytes = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            try { 
                error.printStackTrace(new PrintStream(baos));  
                bytes = baos.toByteArray();
            } catch(Throwable t) {
                t.printStackTrace();
            } finally {
                try { baos.close(); } catch(Throwable t){;} 
            } 
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Error");
            builder.setMessage((bytes==null? null: new String(bytes))+"\n");
            builder.setPositiveButton("  OK  ", approvelistener); 
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        void show(final UIDialog caller, Object message) {
            Context context = caller.getContext();
            if (context == null) return; 
            
            DialogInterface.OnClickListener approvelistener = new DialogInterface.OnClickListener() {	
                public void onClick(DialogInterface dialog, int which) {
                    caller.onApprove();
                }
            }; 
            
            String text = null;
            if (message instanceof Throwable) {
                Throwable t = (Throwable)message;
                text = "[ERROR] failed caused by " + t.getMessage();
            } else {
                text = message+"";
            }
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Information");
            builder.setMessage(text+"\n");
            builder.setPositiveButton("  OK  ", approvelistener); 
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private static class ConfirmationBox 
    {
        void show(final UIDialog caller, Object message) {
            Context context = caller.getContext();
            if (context == null) return; 
            
            DialogInterface.OnClickListener approvelistener = new DialogInterface.OnClickListener() {	
                public void onClick(DialogInterface dialog, int which) {
                    caller.onApprove();
                }
            };
            DialogInterface.OnClickListener cancellistener = new DialogInterface.OnClickListener() {	
                public void onClick(DialogInterface dialog, int which) {
                    caller.onCancel(); 
                } 
            }; 
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setTitle("Confirmation");
            builder.setMessage(message+"\n");
            builder.setPositiveButton(" Yes ", approvelistener);
            builder.setNegativeButton(" No ", cancellistener);
            AlertDialog dialog = builder.create();
            dialog.show();
        } 
    } 
    
    private static class SelectionBox 
    {
        void show(final UIDialog caller, Object[] values) {
            Context context = caller.getContext();
            if (context == null) return;  
            
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {	
                public void onClick(DialogInterface dialog, int which) {
                    caller.onSelectItem(which);
                }
            };

            if (values == null) values = new Object[0];
            final String[] items = new String[values.length];
            for (int i=0; i<items.length; i++) { 
                items[i] = values[i]+""; 
            } 
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select an option");
            builder.setItems(items, listener);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    } 
    
    private static class InputBox 
    {
        void show(final UIDialog caller, Object value, String title) {
            Context context = caller.getContext();
            if (context == null) return;  

            if (title == null) title = "Input";
            
            final EditText editor = new EditText(context);  
            if (value != null) editor.setText(value.toString()); 
           
            DialogInterface.OnClickListener approvelistener = new DialogInterface.OnClickListener() {	
                public void onClick(DialogInterface dialog, int which) {
                    String sv = editor.getText().toString();
                    caller.onApprove(sv.length() == 0? null: sv);
                }
            };
            DialogInterface.OnClickListener cancellistener = new DialogInterface.OnClickListener() {	
                public void onClick(DialogInterface dialog, int which) {
                    caller.onCancel(); 
                } 
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Input");
            builder.setView(editor);
            builder.setPositiveButton("  OK  ", approvelistener);
            builder.setNegativeButton("Cancel", cancellistener);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }       
}
