We want to visualize the instrumented profile tree data.

Use guidelines in @portlet.md for building Portal UIs.

- Create a custom viewer in src/firewrap/tool/portlet/viewers.clj that can be used to view tapped valued in the Portal tool.
- on the left side there will be expandable tree view
    - on each line have preset name, parameter list, number of bwrap rules/args contributed by this preset
    - make the row selectable by clicking on it
    - expanded rows have higher level of indentation like in a tree
- on the right side there will be side panel listing bwrap rules/args for selected preset
    - if no preset is selected it shows all args for the whole profile
- Create components as reagent hiccup, and register and portal viewers

tree data shape:

```
{:symbol nil,
 :location nil,
 :children
 [{:symbol sb/unshare-all,
   :location
   {:file
    "/home/me/projects/firewrap/code/firewrap/test/src/firewrap/sandbox_test.clj",
    :line 19,
    :column 5},
   :children []}
  {:symbol test-preset,
   :location
   {:file
    "/home/me/projects/firewrap/code/firewrap/test/src/firewrap/sandbox_test.clj",
    :line 20,
    :column 5},
   :children
   [{:symbol sb/new-session,
     :location
     {:file
      "/home/me/projects/firewrap/code/firewrap/test/src/firewrap/sandbox_test.clj",
      :line 14,
      :column 5},
     :children []}
    {:symbol sb/bind-ro,
     :location
     {:file
      "/home/me/projects/firewrap/code/firewrap/test/src/firewrap/sandbox_test.clj",
      :line 15,
      :column 5},
     :children []}]}]}
```
