# 在此处添加特定于项目的 ProGuard 规则。
# 您可以使用 build.gradle 中的 proguardFiles 设置来控制应用的配置文件集。
#
# 有关更多详细信息，请参阅
#   http://developer.android.com/guide/developing/tools/proguard.html

# 如果您的项目将 WebView 与 JS 一起使用，请取消注释以下内容，
# 并为 JavaScript 接口类指定完全限定的类名：
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# 取消注释此项以保留用于调试堆栈跟踪的行号信息。
#-keepattributes SourceFile,LineNumberTable

# 如果保留行号信息，请取消注释以隐藏原始源文件名。
#-renamesourcefileattribute SourceFile
