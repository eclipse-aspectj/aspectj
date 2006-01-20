This folder contains a copy of AjcTask from the AspectJ 1.5.1 tree.
It is put on the classpath ahead of aspectjtools.jar when the
property use.local.iajc.task.class is set to true (default = true).
You need this if you want to profile released versions of aspectj
prior to 1.5.1 in order for the <jvmarg> property that the profiler
needs to be supported.