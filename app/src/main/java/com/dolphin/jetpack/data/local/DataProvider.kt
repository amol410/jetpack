// data/local/DataProvider.kt
package com.dolphin.jetpack.data.local

import com.dolphin.jetpack.domain.model.Quiz
import com.dolphin.jetpack.domain.model.Question

object DataProvider {
    // Organized by chapters with 3 Jetpack Compose related quizzes per chapter, each with 10 questions
    val chapterQuizzes = mapOf(
        "Chapter 1" to listOf(
            Quiz(
                id = 1,
                title = "Compose Basics Quiz",
                questions = listOf(
                    Question(
                        text = "What annotation is used to define a composable function?",
                        options = listOf("@Composable", "@Component", "@Compose", "@View"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "In Jetpack Compose, which function is used to set padding?",
                        options = listOf("padding()", "margin()", "inset()", "spacing()"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "Which function is used to create a Column layout in Jetpack Compose?",
                        options = listOf("Column()", "VerticalLayout()", "Stack()", "Box()"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "What is the purpose of the @Preview annotation in Compose?",
                        options = listOf("To preview composables in Android Studio", "To run composables in production", "To cache composables", "To optimize performance"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "Which modifier is used to make a composable clickable?",
                        options = listOf("clickable", "onClick", "clickHandler", "touchable"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "What is the main benefit of Jetpack Compose?",
                        options = listOf("Declarative UI", "Imperative UI", "XML-based UI", "Native UI"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "How do you handle state in Jetpack Compose?",
                        options = listOf("Using state and mutableStateOf", "Using View references", "Using XML attributes", "Using callbacks"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "Which of these is NOT a basic layout in Jetpack Compose?",
                        options = listOf("LinearLayout", "Column", "Row", "Box"),
                        correctAnswerIndex = 0  // LinearLayout is NOT a basic layout in Compose
                    ),
                    Question(
                        text = "What does the 'key' parameter do in Compose?",
                        options = listOf("Helps identify composables across recompositions", "Sets the background", "Defines the color scheme", "Creates animations"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "How is recomposition triggered in Compose?",
                        options = listOf("When state changes", "Every frame", "Only at app start", "When user scrolls"),
                        correctAnswerIndex = 0
                    )
                )
            ),
            Quiz(
                id = 2,
                title = "State Management Quiz",
                questions = listOf(
                    Question(
                        text = "Which function is used to remember a value in Jetpack Compose?",
                        options = listOf("remember()", "save()", "cache()", "store()"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "What is the correct way to create mutable state in Jetpack Compose?",
                        options = listOf("mutableStateOf()", "mutableState()", "createState()", "stateOf()"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "What happens when you update a value in a mutableState?",
                        options = listOf("It triggers recomposition", "Nothing happens", "App crashes", "UI is redrawn from scratch"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which function should you use to remember a value with a calculation?",
                        options = listOf("remember", "rememberSaveable", "rememberUpdatedState", "rememberCoroutineScope"),
                        correctAnswerIndex = 0
                    ),
                    Question(
                        text = "What is the difference between remember and rememberSaveable?",
                        options = listOf("rememberSaveable survives configuration changes", "remember is faster", "remember is more reliable", "No difference"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you hoist state in Compose?",
                        options = listOf("Move state to parent composable", "Move state to child composable", "Remove state", "Add more state"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "What is the purpose of LaunchedEffect in state management?",
                        options = listOf("To run suspend functions in composition", "To create state", "To update UI", "To handle clicks"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which function provides access to ViewModel in Compose?",
                        options = listOf("hiltViewModel", "viewModel", "getViewModel", "useViewModel"),
                        correctAnswerIndex = 1  // viewModel is correct (now at index 1)
                    ),
                    Question(
                        text = "What is the purpose of derived state in Compose?",
                        options = listOf("To create state based on other state", "To delete state", "To hide state", "To duplicate state"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which of these is NOT a proper way to handle user input state?",
                        options = listOf("Directly modifying the state variable", "Creating a callback function", "Using state hoisting", "Passing setter function"),
                        correctAnswerIndex = 0  // First option is NOT proper (it's bad practice)
                    )
                )
            ),
            Quiz(
                id = 3,
                title = "Modifiers Quiz",
                questions = listOf(
                    Question(
                        text = "Which function is used to add click handling in Jetpack Compose?",
                        options = listOf("clickable()", "onClick()", "clickHandler()", "onTap()"),
                        correctAnswerIndex = 0  // clickable() is correct
                    ),
                    Question(
                        text = "How do you apply multiple modifiers to a composable?",
                        options = listOf("Chain them with dots", "Pass them as separate parameters", "Use a modifier array", "Apply them with plus operator"),
                        correctAnswerIndex = 0  // Chain them with dots is correct
                    ),
                    Question(
                        text = "Which modifier is used to set the size of a composable?",
                        options = listOf("size()", "dimension()", "setSize()", "widthHeight()"),
                        correctAnswerIndex = 0  // size() is correct
                    ),
                    Question(
                        text = "What is the purpose of the fillMaxSize() modifier?",
                        options = listOf("Make composable fill available space", "Add padding", "Create animation", "Handle clicks"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which modifier adds padding around a composable?",
                        options = listOf("padding()", "margin()", "inset()", "spacing()"),
                        correctAnswerIndex = 0  // padding() is correct
                    ),
                    Question(
                        text = "How do you align content within a composable?",
                        options = listOf("align()", "gravity()", "position()", "offset()"),
                        correctAnswerIndex = 0  // align() is correct
                    ),
                    Question(
                        text = "Which modifier is used to set background color?",
                        options = listOf("background()", "backgroundColor()", "fillColor()", "setBackground()"),
                        correctAnswerIndex = 0  // background() is correct
                    ),
                    Question(
                        text = "What does the weight() modifier do in a Row?",
                        options = listOf("Distributes space proportionally", "Adds weight text", "Makes it heavier", "Changes font size"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which modifier handles focus events?",
                        options = listOf("focusable()", "clickable()", "enabled()", "interceptFocus()"),
                        correctAnswerIndex = 0  // focusable() is correct
                    ),
                    Question(
                        text = "What is the purpose of combinedModifier in Compose?",
                        options = listOf("To combine multiple modifiers", "To undo modifiers", "To debug modifiers", "To optimize performance"),
                        correctAnswerIndex = 0  // First option is correct
                    )
                )
            )
        ),
        "Chapter 2" to listOf(
            Quiz(
                id = 4,
                title = "Layouts Quiz",
                questions = listOf(
                    Question(
                        text = "Which composable is used to stack elements on top of each other?",
                        options = listOf("Box", "Column", "Row", "Stack"),
                        correctAnswerIndex = 0  // Box is correct
                    ),
                    Question(
                        text = "What is the purpose of LazyColumn in Jetpack Compose?",
                        options = listOf("To efficiently display scrollable lists", "To create column layouts", "To delay rendering", "To group composables"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which layout is used to arrange items horizontally?",
                        options = listOf("Row", "Column", "LinearLayout", "HorizontalBox"),
                        correctAnswerIndex = 0  // Row is correct
                    ),
                    Question(
                        text = "How do you create a scrollable column with better performance?",
                        options = listOf("LazyColumn", "Column with scroll", "ScrollableColumn", "VerticalScrollView"),
                        correctAnswerIndex = 0  // LazyColumn is correct
                    ),
                    Question(
                        text = "What is the difference between Column and LazyColumn?",
                        options = listOf("LazyColumn only composes visible items", "Column is faster", "LazyColumn uses more memory", "No difference"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which layout should you use for a fixed set of items?",
                        options = listOf("Column/Row", "LazyColumn/LazyRow", "Box", "ConstraintLayout"),
                        correctAnswerIndex = 0  // Column/Row is correct for fixed items
                    ),
                    Question(
                        text = "How do you add spacing between items in a Column?",
                        options = listOf("verticalArrangement", "spacing", "itemSpacing", "betweenItems"),
                        correctAnswerIndex = 0  // verticalArrangement is correct
                    ),
                    Question(
                        text = "What is the purpose of ConstraintLayout in Compose?",
                        options = listOf("Create complex layouts with constraints", "Make layouts faster", "Simplify layouts", "Add animations"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you center content in a Box?",
                        options = listOf("align(Alignment.Center)", "centerContent()", "gravity = Center", "use Center composable"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which layout is most efficient for a grid of items?",
                        options = listOf("LazyVerticalGrid", "Grid", "Column with Rows", "ScrollView with Table"),
                        correctAnswerIndex = 0  // LazyVerticalGrid is correct for efficient grids
                    )
                )
            ),
            Quiz(
                id = 5,
                title = "Material Design Quiz",
                questions = listOf(
                    Question(
                        text = "Which component is used to create a button in Jetpack Compose?",
                        options = listOf("Button", "MaterialButton", "ComposeButton", "TextButton"),
                        correctAnswerIndex = 0  // Button is correct
                    ),
                    Question(
                        text = "What is the purpose of Scaffold in Material Design?",
                        options = listOf("To provide structure for typical screens", "To create animations", "To manage state", "To handle navigation"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which component is used for form input fields?",
                        options = listOf("TextField", "EditText", "InputField", "FormInput"),
                        correctAnswerIndex = 0  // TextField is correct
                    ),
                    Question(
                        text = "How do you create a top app bar in Compose?",
                        options = listOf("TopAppBar", "Toolbar", "ActionBar", "AppBar"),
                        correctAnswerIndex = 0  // TopAppBar is correct
                    ),
                    Question(
                        text = "What is the purpose of MaterialTheme?",
                        options = listOf("Define colors, typography, and shapes", "Handle navigation", "Manage state", "Create animations"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which component is used for selection from multiple options?",
                        options = listOf("RadioButton", "CheckBox", "Switch", "Toggle"),
                        correctAnswerIndex = 0  // RadioButton is correct for single selection
                    ),
                    Question(
                        text = "How do you implement bottom navigation?",
                        options = listOf("NavigationBar", "BottomNavigation", "BottomAppBar", "TabRow"),
                        correctAnswerIndex = 0  // NavigationBar is correct in Compose
                    ),
                    Question(
                        text = "Which of these is NOT a Material component?",
                        options = listOf("AndroidView", "Button", "Card", "TextField"),
                        correctAnswerIndex = 0  // AndroidView is NOT a Material component
                    ),
                    Question(
                        text = "What is the purpose of elevation in Material Design?",
                        options = listOf("Show depth and hierarchy", "Improve performance", "Add colors", "Handle clicks"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "Which component is used for a modal bottom sheet?",
                        options = listOf("ModalBottomSheet", "BottomSheet", "BottomModal", "SheetModal"),
                        correctAnswerIndex = 0  // ModalBottomSheet is correct
                    )
                )
            ),
            Quiz(
                id = 6,
                title = "Navigation Quiz",
                questions = listOf(
                    Question(
                        text = "Which library is used for navigation in Jetpack Compose?",
                        options = listOf("Navigation", "ComposeNavigation", "NavController", "Routing"),
                        correctAnswerIndex = 0  // Navigation is correct
                    ),
                    Question(
                        text = "What is the main function used to define navigation graph?",
                        options = listOf("NavHost", "Navigation", "NavGraph", "NavController"),
                        correctAnswerIndex = 0  // NavHost is correct
                    ),
                    Question(
                        text = "How do you navigate to a destination in Compose?",
                        options = listOf("navigator.navigate()", "NavHost.navigate()", "Navigation.navigate()", "NavController.navigateTo()"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you pass data between destinations?",
                        options = listOf("navArgs", "intent extras", "bundle", "state"),
                        correctAnswerIndex = 0  // navArgs is correct
                    ),
                    Question(
                        text = "What is the purpose of NavGraph?",
                        options = listOf("Define navigation structure", "Create animations", "Manage state", "Handle UI"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you handle back navigation?",
                        options = listOf("navigator.popBackStack()", "goBack()", "navigateUp()", "backPress()"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "What is the purpose of rememberNavController()?",
                        options = listOf("Create NavController instance", "Store navigation history", "Handle navigation events", "Define destinations"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you define a composable destination?",
                        options = listOf("composable() function", "destination() function", "route() function", "screen() function"),
                        correctAnswerIndex = 0  // composable() function is correct
                    ),
                    Question(
                        text = "What is the purpose of navigation arguments?",
                        options = listOf("Pass data to destinations", "Change navigation behavior", "Add animations", "Handle errors"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you handle deep linking in Compose navigation?",
                        options = listOf("Add deepLink to composable", "Use Intent filters", "Define URI patterns", "Set up web URLs"),
                        correctAnswerIndex = 0  // First option is correct
                    )
                )
            )
        ),
        "Chapter 3" to listOf(
            Quiz(
                id = 7,
                title = "Theming Quiz",
                questions = listOf(
                    Question(
                        text = "Which composable is used to define the theme in Jetpack Compose?",
                        options = listOf("MaterialTheme", "Theme", "AppTheme", "ComposeTheme"),
                        correctAnswerIndex = 0  // MaterialTheme is correct
                    ),
                    Question(
                        text = "What is used to access the current theme colors?",
                        options = listOf("MaterialTheme.colorScheme", "Theme.colors", "AppTheme.colors", "ComposeTheme.colors"),
                        correctAnswerIndex = 0  // MaterialTheme.colorScheme is correct
                    ),
                    Question(
                        text = "How do you define typography in Jetpack Compose?",
                        options = listOf("Typography", "FontStyle", "TextTypes", "TextStyles"),
                        correctAnswerIndex = 0  // Typography is correct
                    ),
                    Question(
                        text = "What is the purpose of a ColorScheme?",
                        options = listOf("Define semantic colors", "Set background", "Create gradients", "Animate colors"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you create a custom color palette?",
                        options = listOf("ColorScheme constructor", "ColorProvider", "PaletteBuilder", "ColorFactory"),
                        correctAnswerIndex = 0  // ColorScheme constructor is correct
                    ),
                    Question(
                        text = "What is the recommended way to define shapes?",
                        options = listOf("Shape system", "Corner size", "Rounded corners", "Shape constructor"),
                        correctAnswerIndex = 0  // Shape system is correct
                    ),
                    Question(
                        text = "How do you access theme values in composables?",
                        options = listOf("MaterialTheme", "Theme.current", "currentTheme", "LocalTheme"),
                        correctAnswerIndex = 0  // MaterialTheme is correct
                    ),
                    Question(
                        text = "What is the purpose of DynamicColors?",
                        options = listOf("Use system wallpaper colors", "Animate colors", "Create gradients", "Save memory"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you support dark theme?",
                        options = listOf("Dark configuration", "Night mode", "Dark color scheme", "Theme toggle"),
                        correctAnswerIndex = 0  // Dark color scheme is correct
                    ),
                    Question(
                        text = "What does LocalContentColor provide?",
                        options = listOf("Default content color", "Background color", "Accent color", "Primary color"),
                        correctAnswerIndex = 0  // First option is correct
                    )
                )
            ),
            Quiz(
                id = 8,
                title = "Animation Quiz",
                questions = listOf(
                    Question(
                        text = "Which function is used for basic animations in Jetpack Compose?",
                        options = listOf("animate*As()", "Animation", "AnimatedValue", "Animate"),
                        correctAnswerIndex = 0  // animate*As() is correct (like animateFloatAsState, etc.)
                    ),
                    Question(
                        text = "What is the purpose of AnimatedVisibility?",
                        options = listOf("To animate the appearance and disappearance of composables", "To animate visibility changes", "To create fade animations", "To manage animation state"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you create a color animation?",
                        options = listOf("animateColorAsState()", "ColorAnimation()", "AnimatedColor()", "ColorAnimator()"),
                        correctAnswerIndex = 0  // animateColorAsState() is correct
                    ),
                    Question(
                        text = "What is the purpose of Transition in Compose animations?",
                        options = listOf("Coordinate multiple animations", "Create basic animations", "Handle state", "Manage UI"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you animate size changes?",
                        options = listOf("animateDpAsState", "animateSizeAsState", "animateDimensionAsState", "animateBoundsAsState"),
                        correctAnswerIndex = 0  // animateDpAsState is correct for size dimensions (index 0)
                    ),
                    Question(
                        text = "Which of these is NOT an animation API in Compose?",
                        options = listOf("SpringAnimation", "AnimationSpec", "InfiniteTransition", "Transition"),
                        correctAnswerIndex = 0  // SpringAnimation is NOT an API (SpringSpec is)
                    ),
                    Question(
                        text = "What does the AnimationSpec define?",
                        options = listOf("Animation behavior and timing", "UI components", "Data models", "Navigation routes"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you run multiple animations together?",
                        options = listOf("Multiple animate*AsState calls", "InfiniteTransition", "AnimatedVisibility", "AnimationSequence"),
                        correctAnswerIndex = 1  // InfiniteTransition is correct (now at index 1)
                    ),
                    Question(
                        text = "What is the purpose of AnimationVector?",
                        options = listOf("Represent animated values with multiple properties", "Vector graphics", "Animation paths", "Position tracking"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you handle animation state?",
                        options = listOf("AnimateState", "AnimationState", "CurrentAnimation", "AnimationController"),
                        correctAnswerIndex = 1  // AnimationState is correct (now at index 1)
                    )
                )
            ),
            Quiz(
                id = 9,
                title = "ViewModel Quiz",
                questions = listOf(
                    Question(
                        text = "Which function is used to access ViewModel in Compose?",
                        options = listOf("viewModel()", "getViewModel()", "hiltViewModel()", "composeViewModel()"),
                        correctAnswerIndex = 0  // viewModel() is correct
                    ),
                    Question(
                        text = "What is the benefit of using ViewModel with Jetpack Compose?",
                        options = listOf("To separate UI logic from UI", "To improve performance", "To manage themes", "To handle navigation"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you observe ViewModel state in Compose?",
                        options = listOf("Observe state using state variables", "Use direct property access", "ViewModel automatically updates UI", "Compose handles it automatically"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "What is the purpose of StateFlow in ViewModel?",
                        options = listOf("To emit state updates", "To store data", "To handle events", "To manage UI"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you handle events in ViewModel with Compose?",
                        options = listOf("Use SharedFlow or callbacks", "Direct state updates", "Modify UI directly", "Use LiveEvent"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "What happens to ViewModel during configuration changes?",
                        options = listOf("It survives", "It's destroyed", "It's paused", "It's copied"),
                        correctAnswerIndex = 0  // Survives is correct
                    ),
                    Question(
                        text = "How do you test ViewModel with Compose?",
                        options = listOf("Unit tests for business logic", "UI tests", "Integration tests", "Compose tests"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "What is the purpose of SavedStateHandle in ViewModel?",
                        options = listOf("Preserve state across process death", "Save app data", "Cache images", "Store preferences"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "How do you handle async operations in ViewModel?",
                        options = listOf("Use.viewModelScope.launch", "Direct coroutines", "Callback functions", "AsyncTask"),
                        correctAnswerIndex = 0  // First option is correct
                    ),
                    Question(
                        text = "What is the difference between State and Event in ViewModel?",
                        options = listOf("State is persistent, events are one-time", "No difference", "State is UI, events are data", "State is faster"),
                        correctAnswerIndex = 0  // First option is correct
                    )
                )
            )
        )
    )
    
    // Flattened list for backward compatibility
    val quizList = chapterQuizzes.values.flatten()
}