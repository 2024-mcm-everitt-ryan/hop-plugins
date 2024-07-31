package ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import static ie.dcu.mcm.hop.pipeline.transforms.llm.chat.internals.ui.i18nUtil.i18n;
import static org.eclipse.swt.SWT.PUSH;
import static org.eclipse.swt.SWT.Selection;

public class OkButton {
    private final Button delegate;

    public OkButton(Composite parent, Listener listener) {
        this.delegate = new Button(parent, PUSH);
        this.delegate.setText(i18n("System.Button.OK"));
        this.delegate.addListener(Selection, listener);
    }

    public Button delegate() {
        return delegate;
    }
}
