package com.dolphin.jetpack

data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class Quiz(
    val title: String,
    val questions: List<Question>
)

object DataProvider {

    val quizList = listOf(
        Quiz(
            title = "Jetpack Compose Basics",
            questions = listOf(
                Question(
                    "What is the main building block for UI in Jetpack Compose?",
                    listOf("View", "Activity", "Composable", "XML"),
                    2,
                    "A 'Composable' is a function that describes a part of your UI. It's the fundamental building block."
                ),
                Question(
                    "Which layout composable arranges items vertically?",
                    listOf("Row", "Column", "Box", "Spacer"),
                    1,
                    "'Column' arranges its children in a vertical sequence."
                ),
                Question(
                    "What is the 'onClick' parameter used for in a Button?",
                    listOf("To set the text", "To change the color", "To handle clicks", "To set the size"),
                    2,
                    "'onClick' is a lambda function that gets executed when the user taps the button."
                ),
                Question(
                    "How do you add space between two composables?",
                    listOf("Spacer", "Padding", "Margin", "Space"),
                    0,
                    "The 'Spacer' composable is specifically designed to create empty space between other composables."
                ),
                Question(
                    "What does 'remember' do in Compose?",
                    listOf("Forgets a value", "Stores a value in a database", "Stores a value in memory", "Remembers the user"),
                    2,
                    "'remember' stores a value in memory across recompositions, allowing Compose to maintain state."
                ),
                Question(
                    "Which Composable is used for an efficient scrollable list?",
                    listOf("Column", "Row", "LazyColumn", "Box"),
                    2,
                    "'LazyColumn' is the efficient way to display a potentially large, scrollable list of items vertically. It only renders items visible on screen."
                ),
                Question(
                    "What is 'Modifier' used for?",
                    listOf("To change logic", "To decorate a composable", "To store data", "To run tasks"),
                    1,
                    "Modifiers (e.g., Modifier.padding, Modifier.fillMaxSize) are used to decorate or add behavior to composables."
                ),
                Question(
                    "How do you display text on the screen?",
                    listOf("Text()", "TextView()", "Label()", "String()"),
                    0,
                    "The 'Text' composable function is used to display a string of text."
                ),
                Question(
                    "Which layout stacks its children on top of each other?",
                    listOf("Row", "Column", "Box", "Surface"),
                    2,
                    "'Box' arranges its children on top of one another, with the last child being on top. It's great for overlays."
                ),
                Question(
                    "What is 'recomposition'?",
                    listOf("A database query", "Re-running composables", "An error", "Deleting UI"),
                    1,
                    "Recomposition is the process of Compose re-running your composable functions to build an updated UI when state changes."
                )
            )
        ),

        Quiz(
            title = "Compose Layouts & Modifiers",
            questions = listOf(
                Question(
                    "Which layout arranges children in a horizontal sequence?",
                    listOf("Column", "Row", "Box", "LazyRow"),
                    1,
                    "'Row' arranges its children horizontally. 'LazyRow' is for large scrollable horizontal lists."
                ),
                Question(
                    "How do you make a composable take up all available space?",
                    listOf("Modifier.fillMaxSize()", "Modifier.fillWidth()", "Modifier.wrapContent()", "Modifier.size(Max)"),
                    0,
                    "'Modifier.fillMaxSize()' makes the composable expand to fill both the width and height of its parent."
                ),
                Question(
                    "What modifier adds space *inside* a composable's borders?",
                    listOf("Modifier.margin()", "Modifier.spacer()", "Modifier.padding()", "Modifier.border()"),
                    2,
                    "'Modifier.padding()' adds space inside the composable's boundaries, pushing the content inwards."
                ),
                Question(
                    "What does `Arrangement.SpaceBetween` do in a `Row`?",
                    listOf("Puts space around items", "Puts space between items", "Packs items at the start", "Packs items at the center"),
                    1,
                    "'Arrangement.SpaceBetween' places children with equal space between them, but no space at the start or end."
                ),
                Question(
                    "What does `Alignment.CenterVertically` do in a `Row`?",
                    listOf("Centers the Row", "Centers children horizontally", "Centers children vertically", "Centers text"),
                    2,
                    "In a 'Row', 'Alignment.CenterVertically' aligns the children to the vertical center of the Row."
                ),
                Question(
                    "How do you add a 2dp red border to a `Text`?",
                    listOf("Modifier.border(2.dp, Color.Red)", "Modifier.padding(2.dp, Color.Red)", "Modifier.background(Color.Red)", "Modifier.outline(2.dp, Color.Red)"),
                    0,
                    "The 'Modifier.border()' is used to add a border with a specific width and color. You'll need to import androidx.compose.ui.graphics.Color."
                ),
                Question(
                    "What is the purpose of `Modifier.weight(1f)` in a `Column`?",
                    listOf("Makes it heavy", "Sets its weight to 1kg", "Makes it 1 pixel high", "Makes it take remaining space"),
                    3,
                    "The 'weight()' modifier makes a composable fill a proportional amount of the available space. A weight of '1f' will make it take all remaining space if it's the only weighted child."
                ),
                Question(
                    "Which Composable is used for a grid layout?",
                    listOf("LazyVerticalGrid", "Column", "Row", "BoxWithConstraints"),
                    0,
                    "'LazyVerticalGrid' is the efficient composable for displaying items in a scrollable grid."
                ),
                Question(
                    "How do you make an `Image` circular?",
                    listOf("Modifier.clip(CircleShape)", "Modifier.round(100)", "Modifier.circle()", "Modifier.shape(Circular)"),
                    0,
                    "'Modifier.clip(CircleShape)' will clip the composable (like an Image) to the shape of a circle. You'll need to import androidx.compose.foundation.shape.CircleShape."
                ),
                Question(
                    "How do you add 16dp of space *around the outside* of a Button?",
                    listOf("Modifier.padding(16.dp)", "Modifier.margin(16.dp)", "Modifier.border(16.dp)", "Modifier.offset(16.dp)"),
                    0,
                    "In Compose, `Modifier.padding()` is used to add space. When applied, it adds space around the element, which acts like a 'margin' and spaces it away from other elements."
                )
            )
        ),

        Quiz(
            title = "Compose State & Events",
            questions = listOf(
                Question(
                    "What function is used to create a mutable state object in Compose?",
                    listOf("remember { stateOf() }", "val state = ...", "mutableStateOf()", "remember { mutableStateOf() }"),
                    3,
                    "'remember { mutableStateOf(initialValue) }' creates a state object that Compose 'remembers' across recompositions."
                ),
                Question(
                    "What is the `by` keyword used for with state? (e.g., `var text by ...`)",
                    listOf("Delegated property", "To bypass the state", "A function call", "To delete state"),
                    0,
                    "The 'by' keyword uses property delegation, allowing you to read/write the state's value directly (`text`) instead of using `.value` (`text.value`)."
                ),
                Question(
                    "What is 'State Hoisting'?",
                    listOf("Lifting state up", "Deleting state", "Making state complex", "Hiding state"),
                    0,
                    "State hoisting is a pattern of moving state from a composable to its caller, making the composable stateless and more reusable."
                ),
                Question(
                    "Which composable is a pre-built text input field?",
                    listOf("Text()", "TextField()", "InputBox()", "EditText()"),
                    1,
                    "'TextField()' (and 'OutlinedTextField()') are the standard composables for getting text input from a user."
                ),
                Question(
                    "What are the two core parameters of a `TextField`?",
                    listOf("text, onTextChange", "value, onValueChange", "string, onStringChange", "data, onDataChange"),
                    1,
                    "A controlled `TextField` requires a `value` (the current text to display) and an `onValueChange` lambda (what to do when the user types)."
                ),
                Question(
                    "What does `rememberSaveable` do?",
                    listOf("Saves state to a file", "Saves state across configuration changes", "Saves state to the cloud", "Saves the user"),
                    1,
                    "'rememberSaveable' is like 'remember', but it also saves the state across Activity/Process recreation (e.g., screen rotation)."
                ),
                Question(
                    "How do you handle a click on an `Image`?",
                    listOf("Image(onClick = ...)", "Image(modifier = Modifier.clickable { ... })", "Image(onTap = ...)", "ClickableImage(...)"),
                    1,
                    "You use 'Modifier.clickable { ... }' to make almost any composable respond to clicks."
                ),
                Question(
                    "What is a `ViewModel` used for in Compose?",
                    listOf("To draw the UI", "To hold business logic and state", "To navigate", "To style the app"),
                    1,
                    "A 'ViewModel' is part of Android Architecture Components. It's used to hold and manage UI-related state and business logic, surviving configuration changes."
                ),
                Question(
                    "How do you create a `Button` that *cannot* be clicked?",
                    listOf("Button(enabled = false)", "Button(clickable = false)", "DisabledButton()", "Button(onClick = null)"),
                    0,
                    "The `enabled` parameter of a `Button` (set to `false`) controls whether it can be interacted with and changes its visual appearance."
                ),
                Question(
                    "What is a `LaunchedEffect` used for?",
                    listOf("To launch a new screen", "To run suspend functions", "To animate a composable", "To log an effect"),
                    1,
                    "'LaunchedEffect' is a side-effect composable used to run suspend functions (like network calls) safely within the scope of a composable."
                )
            )
        ),

        Quiz(
            title = "Compose Navigation & UI",
            questions = listOf(
                Question(
                    "What is the official library for navigation in Compose?",
                    listOf("Compose Navigator", "Navigation-Compose", "Activity Navigator", "Compose Router"),
                    1,
                    "'Navigation-Compose' is the official library that integrates the Jetpack Navigation component with Jetpack Compose."
                ),
                Question(
                    "What does a `NavHostController` do?",
                    listOf("Hosts the UI", "Controls the network", "Controls navigation actions", "Styles the app"),
                    2,
                    "The 'NavHostController' is the central API for navigation. You use it to navigate() to new screens or go popBackStack()."
                ),
                Question(
                    "What composable defines the navigation graph?",
                    listOf("NavGraph()", "Navigation()", "NavHost()", "Router()"),
                    2,
                    "The 'NavHost' composable links the 'NavHostController' with a navigation graph, defining all the 'composable()' destinations."
                ),
                Question(
                    "How do you define a screen in a `NavHost`?",
                    listOf("screen(route = ...) { ... }", "destination(...) { ... }", "composable(route = ...) { ... }", "route(...) { ... }"),
                    2,
                    "You use the `composable()` builder function inside the `NavHost` to define a single destination (screen) and its unique string `route`."
                ),
                Question(
                    "What composable provides a standard app bar at the top?",
                    listOf("AppBar()", "TopBar()", "Scaffold()", "TopAppBar()"),
                    3,
                    "'TopAppBar()' is the Material Design composable for a top app bar. It's often used within the `topBar` slot of a `Scaffold`."
                ),
                Question(
                    "What is a `Scaffold` composable?",
                    listOf("A code template", "A pre-built layout structure", "A type of database", "An error screen"),
                    1,
                    "'Scaffold' implements the basic Material Design layout structure, providing slots for a `TopAppBar`, `BottomAppBar`, `FloatingActionButton`, etc."
                ),
                Question(
                    "How do you show a 'toast' message in Compose?",
                    listOf("Toast.makeText(...).show()", "Snackbar(...)", "Toast(...)", "ShowMessage(...)"),
                    0,
                    "Showing a `Toast` is still done the 'old' Android way. You get the `Context` (e.g., from `LocalContext.current`) and call `Toast.makeText(context, ...).show()`."
                ),
                Question(
                    "What is the Material Design way to show a brief message?",
                    listOf("Snackbar", "AlertDialog", "Popup", "Notification"),
                    0,
                    "A 'Snackbar' is the Material Design component for showing brief messages. You show it using a `SnackbarHostState` within a `Scaffold`."
                ),
                Question(
                    "Which composable shows a 'Yes/No' dialog?",
                    listOf("Dialog()", "Popup()", "AlertDialog()", "ConfirmDialog()"),
                    2,
                    "'AlertDialog' is a pre-built composable that shows a dialog with a title, text, and one or more buttons (e.g., 'Confirm', 'Dismiss')."
                ),
                Question(
                    "How do you navigate *with* an argument (e.g., a user ID)?",
                    listOf("navigate(\"profile/\$userId\")", "navigate(\"profile\", userId)", "navigate(Screen.Profile(userId))", "sendArgument(...)"),
                    0,
                    "You pass arguments as part of the string route (e.g., \"profile/123\"), and define the argument's key in the composable() builder (e.g., route = \"profile/{userId}\")."
                )
            )
        ),

        Quiz(
            title = "Advanced Compose",
            questions = listOf(
                Question(
                    "What is `LazyColumn`'s `key` parameter used for?",
                    listOf("To unlock the list", "To improve performance", "To set a password", "To filter the list"),
                    1,
                    "Providing a stable and unique `key` for each item helps Compose optimize recomposition and maintain scroll position when the list changes."
                ),
                Question(
                    "What is the main purpose of `BoxWithConstraints`?",
                    listOf("To add borders", "To get parent's size constraints", "To constrain children", "To create a 3D box"),
                    1,
                    "It's a layout that provides you with the `minWidth`, `maxWidth`, `minHeight`, and `maxHeight` constraints from the parent, letting you build responsive UI."
                ),
                Question(
                    "How can you create a custom layout in Compose?",
                    listOf("By using the `Layout` composable", "By editing the source code", "It's not possible", "By using XML"),
                    0,
                    "The 'Layout' composable is the foundation of all layouts. You provide a lambda that measures and places children manually."
                ),
                Question(
                    "What is a `CompositionLocal`?",
                    listOf("A local variable", "A way to pass data down the UI tree", "A local database", "A composable function"),
                    1,
                    "'CompositionLocal' allows you to pass data (like the current theme) down the composable tree implicitly, without passing it as a parameter to every function."
                ),
                Question(
                    "What is `MaterialTheme.colorScheme` an example of?",
                    listOf("A `ViewModel`", "A `CompositionLocal`", "A `Database`", "A `network request`"),
                    1,
                    "The Material Theme system uses `CompositionLocal` to provide `colorScheme`, `typography`, and `shapes` to all composables below it."
                ),
                Question(
                    "How do you run an animation in Compose?",
                    listOf("animate*AsState", "Animate(...)", "Animation(...)", "startAnimation()"),
                    0,
                    "Functions like 'animateFloatAsState' or 'animateColorAsState' are the simplest way to animate a value change between two states."
                ),
                Question(
                    "What is `Crossfade` used for?",
                    listOf("To fade to black", "To animate between two layouts", "To blur an image", "To cross-reference code"),
                    1,
                    "'Crossfade' is a simple composable that animates between two different composable contents with a crossfade (fade-out, fade-in) effect."
                ),
                Question(
                    "What is a 'side-effect' in Compose?",
                    listOf("An error", "A UI bug", "Code that affects things outside Compose", "A background color"),
                    2,
                    "A side-effect is a change to the app's state that happens outside the scope of a composable function, like launching a coroutine or updating a `ViewModel`."
                ),
                Question(
                    "What is `LaunchedEffect(Unit)` used for?",
                    listOf("To run code once when the composable enters", "To do nothing", "To launch a new app", "To fail the effect"),
                    0,
                    "Using `Unit` as the key means the effect will run only once when the composable is first added to the screen (composed) and won't re-run."
                ),
                Question(
                    "How do you test a Composable function?",
                    listOf("With `createComposeRule()`", "With `Espresso`", "With `Robolectric`", "You can't test them"),
                    0,
                    "You use a test rule like 'createComposeRule()' or 'createAndroidComposeRule()' to host your composable in a test environment and find/interact with nodes."
                )
            )
        )
    )
}