---
name: Material Design 3 Implementation
description: Guidelines and patterns for implementing Material Design 3 in Android apps
category: ui
applies_to:
  - "**/res/layout/*.xml"
  - "**/ui/**/*.kt"
  - "**/*Activity.kt"
  - "**/*Fragment.kt"
---

# Material Design 3 Implementation Skill

This skill provides guidelines and code examples for implementing Material Design 3 components in Android apps.

## Theme Setup

### Material Theme in themes.xml

```xml
<resources>
    <!-- Base application theme extending Material3 -->
    <style name="Theme.NovaChat" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Primary colors -->
        <item name="colorPrimary">@color/md_theme_primary</item>
        <item name="colorOnPrimary">@color/md_theme_on_primary</item>
        <item name="colorPrimaryContainer">@color/md_theme_primary_container</item>
        <item name="colorOnPrimaryContainer">@color/md_theme_on_primary_container</item>
        
        <!-- Secondary colors -->
        <item name="colorSecondary">@color/md_theme_secondary</item>
        <item name="colorOnSecondary">@color/md_theme_on_secondary</item>
        
        <!-- Surface colors -->
        <item name="colorSurface">@color/md_theme_surface</item>
        <item name="colorOnSurface">@color/md_theme_on_surface</item>
        
        <!-- Error colors -->
        <item name="colorError">@color/md_theme_error</item>
        <item name="colorOnError">@color/md_theme_on_error</item>
    </style>
</resources>
```

## Common Material Components

### MaterialButton

```xml
<!-- Filled button (primary action) -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/primaryButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/send"
    style="@style/Widget.Material3.Button" />

<!-- Outlined button (secondary action) -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/secondaryButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/cancel"
    style="@style/Widget.Material3.Button.OutlinedButton" />

<!-- Text button (tertiary action) -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/tertiaryButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/skip"
    style="@style/Widget.Material3.Button.TextButton" />
```

### TextInputLayout

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/messageInputLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/message_hint"
    app:errorEnabled="true"
    app:counterEnabled="true"
    app:counterMaxLength="280"
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox">
    
    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/messageInput"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="textMultiLine"
        android:maxLines="4" />
        
</com.google.android.material.textfield.TextInputLayout>
```

### MaterialCardView

```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardElevation="2dp"
    app:cardCornerRadius="12dp"
    app:strokeWidth="1dp"
    app:strokeColor="?attr/colorOutline">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- Card content -->
        
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

### FloatingActionButton

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fab"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="@string/send_message"
    app:srcCompat="@drawable/ic_send"
    app:layout_anchor="@id/messageInputLayout"
    app:layout_anchorGravity="bottom|end" />

<!-- Extended FAB with text -->
<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    android:id="@+id/extendedFab"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/new_chat"
    app:icon="@drawable/ic_add" />
```

### BottomNavigationView

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottomNavigation"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:menu="@menu/bottom_navigation" />

<!-- menu/bottom_navigation.xml -->
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_chats"
        android:icon="@drawable/ic_chat"
        android:title="@string/chats" />
    <item
        android:id="@+id/nav_contacts"
        android:icon="@drawable/ic_contacts"
        android:title="@string/contacts" />
    <item
        android:id="@+id/nav_settings"
        android:icon="@drawable/ic_settings"
        android:title="@string/settings" />
</menu>
```

### TopAppBar

```xml
<com.google.android.material.appbar.MaterialToolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    app:title="@string/app_name"
    app:navigationIcon="@drawable/ic_menu" />
```

## Layout Best Practices

### Use ConstraintLayout for Complex Layouts

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Use constraints instead of nested layouts -->
    <TextView
        android:id="@+id/title"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

### Dimension Resources

```xml
<!-- values/dimens.xml -->
<resources>
    <!-- Spacing -->
    <dimen name="spacing_xs">4dp</dimen>
    <dimen name="spacing_small">8dp</dimen>
    <dimen name="spacing_medium">16dp</dimen>
    <dimen name="spacing_large">24dp</dimen>
    <dimen name="spacing_xl">32dp</dimen>
    
    <!-- Touch targets -->
    <dimen name="touch_target_min">48dp</dimen>
    
    <!-- Corner radius -->
    <dimen name="corner_radius_small">4dp</dimen>
    <dimen name="corner_radius_medium">8dp</dimen>
    <dimen name="corner_radius_large">12dp</dimen>
</resources>
```

## Accessibility

### Content Descriptions

```xml
<ImageButton
    android:id="@+id/sendButton"
    android:layout_width="@dimen/touch_target_min"
    android:layout_height="@dimen/touch_target_min"
    android:contentDescription="@string/send_message"
    android:src="@drawable/ic_send" />
```

### Minimum Touch Targets

```xml
<!-- Ensure all clickable elements are at least 48dp x 48dp -->
<Button
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:minWidth="@dimen/touch_target_min"
    android:minHeight="@dimen/touch_target_min" />
```

## Color Contrast

Use semantic colors that automatically adapt:
- `?attr/colorPrimary` instead of `@color/blue`
- `?attr/colorOnPrimary` for text on primary color
- `?attr/colorSurface` for backgrounds
- `?attr/colorOnSurface` for text on surfaces

## Dark Theme Support

```xml
<!-- values/colors.xml (light theme) -->
<resources>
    <color name="md_theme_primary">#6200EE</color>
    <color name="md_theme_on_primary">#FFFFFF</color>
</resources>

<!-- values-night/colors.xml (dark theme) -->
<resources>
    <color name="md_theme_primary">#BB86FC</color>
    <color name="md_theme_on_primary">#000000</color>
</resources>
```

## Motion and Animation

### Material Motion Patterns

```kotlin
// Shared element transitions
val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
    activity,
    imageView,
    "shared_image"
)
startActivity(intent, options.toBundle())
```

### Material Animations

```kotlin
// Fade in/out
MaterialFade().apply {
    duration = 300
    addTarget(view)
}.let { TransitionManager.beginDelayedTransition(container, it) }
```

## Typography

```xml
<!-- Use Material Typography scale -->
<TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textAppearance="?attr/textAppearanceHeadlineLarge" />

<!-- Available text appearances:
    - textAppearanceDisplayLarge/Medium/Small
    - textAppearanceHeadlineLarge/Medium/Small
    - textAppearanceTitleLarge/Medium/Small
    - textAppearanceBodyLarge/Medium/Small
    - textAppearanceLabelLarge/Medium/Small
-->
```

## Common Mistakes to Avoid

1. ❌ Using deprecated support library components
   - Use `com.google.android.material.*` not `android.support.*`

2. ❌ Hardcoding colors and dimensions
   - Use theme attributes and resource files

3. ❌ Not providing content descriptions
   - Always add contentDescription for images/icons

4. ❌ Touch targets smaller than 48dp
   - Use minWidth and minHeight

5. ❌ Using findViewById
   - Use ViewBinding instead

6. ❌ Ignoring dark theme
   - Always test in both light and dark modes
