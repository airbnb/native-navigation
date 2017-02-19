package com.airbnb.android.react.navigation;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.facebook.react.bridge.ReadableMap;

import java.util.Map;

public class DefaultNavigationImplementation implements NavigationImplementation {

  // TODO(lmr): do we want to pass in previous properties here as well?
  public void reconcileNavigationProperties(
      ReactInterface component,
      ReactToolbar toolbar,
//      Map<String, Object> properties,
      ReadableMap properties
  ) {
    Log.d("Implementation", "reconcileNavigationProperties");
    boolean invalidateMenu = false;
    if (properties.hasKey("title")) {
      String title = properties.getString("title");
      toolbar.setTitle(title);
    }

    if (properties.hasKey("rightTitle")) {
      invalidateMenu = true;
    }

    if (properties.hasKey("backgroundColor")) {
      Integer backgroundColor = properties.getInt("backgroundColor");
      toolbar.setBackgroundColor(backgroundColor);
    }

    if (properties.hasKey("foregroundColor")) {
      Integer foregroundColor = properties.getInt("foregroundColor");
      toolbar.setForegroundColor(foregroundColor);
    }

    if (properties.hasKey("titleColor")) {
      Integer titleColor = properties.getInt("titleColor");
      toolbar.setTitleTextColor(titleColor);
    }

    if (properties.hasKey("elevation")) {
      Double elevation = properties.getDouble("elevation");
      toolbar.setElevation(elevation.floatValue());
    }

    if (properties.hasKey("alpha")) {
      Double alpha = properties.getDouble("alpha");
      toolbar.setElevation(alpha.floatValue());
    }

    if (properties.hasKey("subtitle")) {
      String subtitle = properties.getString("subtitle");
      toolbar.setSubtitle(subtitle);
    }

    if (properties.hasKey("navIcon")) {
      toolbar.setNavIconSource(properties.getMap("navIcon"));
    }

//    toolbar.setLogo(drawable);
//    toolbar.setNavigationIcon(drawable);
//    toolbar.setTextAlignment(int);
//    toolbar.setTextDirection(int);
//    toolbar.setSubtitleTextColor(int);
//    toolbar.setForeground(drawable);
//    toolbar.setCameraDistance(0.1);
//    toolbar.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
//    toolbar.setForegroundTintMode(PorterDuff.Mode.DARKEN);
//    toolbar.setEnabled(true);

    if (invalidateMenu) {
      component.getActivity().supportInvalidateOptionsMenu();
    }
  }

  public void createOptionsMenu(
      final ReactInterface component,
      ReactToolbar toolbar,
//      Map<String, Object> properties,
      ReadableMap properties,
      Menu menu
  ) {
    if (properties.hasKey("rightTitle")) {
      String rightTitle = properties.getString("rightTitle");
      MenuItem item = menu.add(rightTitle);
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          component.emitEvent("onRightPress", null);
          return true;
        }
      });
    }
  }

  public boolean onOptionsItemSelected(
      final ReactInterface component,
      ReactToolbar toolbar,
//      Map<String, Object> properties,
      ReadableMap properties,
      MenuItem item
  ) {
    // TODO(lmr): we need to make this possible somehow
//    if (item.getItemId() == android.R.id.home) {
//      component.emitEvent(ON_LEFT_PRESS, null);
//      if (reactNavigationCoordinator.getDismissCloseBehavior(component)) {
//        component.dismiss();
//        return true; // consume the event
//      } else {
//        return super.onOptionsItemSelected(item);
//      }
//    }

    component.emitEvent("onRightPress", null);
    return false;
  }

  //    interface OnMenuButtonClickListener {
//        /**
//         * @param button The selected button.
//         * @param index  The position of the button in the toolbar.
//         */
//        void onClick(MenuButton button, int index);
//    }

  /** Adds all the buttons to the given menu. Uses the given click listener as a callback for when any button is selected. */
//    static void addButtonsToMenu(Context context, Menu menu, List<MenuButton> buttons, OnMenuButtonClickListener onClickListener) {
//        for (int i = 0; i < buttons.size(); i++) {
//            MenuButton button = buttons.get(i);
//            MenuItem item = menu.add(button.title);
//            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//            final int buttonIndex = i;
//            if (button.useForegroundColor) {
//                item.setIcon(button.icon);
//                item.setOnMenuItemClickListener(menuItem -> {
//                    onClickListener.onClick(button, buttonIndex);
//                    return true;
//                });
//            } else {
//                // Uses a linear layout to provide layout bounds. This is copied from what MenuButton does internally if a layout resource is set.
//                ReactMenuItemView itemView =
//                        (ReactMenuItemView) LayoutInflater.from(context).inflate(R.layout.menu_item_view, new LinearLayout(context), false);
//                itemView.setImageResource(button.icon);
//                itemView.setOnClickListener(v -> onClickListener.onClick(button, buttonIndex));
//                itemView.setContentDescription(context.getString(button.title));
//                item.setActionView(itemView);
//            }
//        }
//    }
}
