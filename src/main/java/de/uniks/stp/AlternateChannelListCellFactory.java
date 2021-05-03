package de.uniks.stp;

import de.uniks.stp.model.Channel;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class AlternateChannelListCellFactory implements javafx.util.Callback<javafx.scene.control.ListView<de.uniks.stp.model.Channel>, javafx.scene.control.ListCell<de.uniks.stp.model.Channel>> {
    /**
     * The <code>call</code> method is called when required, and is given a
     * single argument of type P, with a requirement that an object of type R
     * is returned.
     *
     * @param param The single argument upon which the returned value should be
     *              determined.
     * @return An object of type R that may be determined based on the provided
     * parameter value.
     */
    @Override
    public ListCell<Channel> call(ListView<Channel> param) {
        return new ChannelListCell();
    }

    private static class UserListCell extends ListCell<Channel> {
        protected void updateItem(Channel item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item.isStatus()) {
                    this.setText(item.getName());
                }
            }
        }
    }
}
