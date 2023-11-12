# Electric Datavis Gallery

I wanted to perform some data visualisation and found it difficult to find reference sources - especially in Clojurescript. 

The Gold Standard for web based data visualisations is [D3](https://d3js.org/) but this was also created at a time when DOM management was very manual and tedius. The state of Javascript and DOM management has improved a lot. Most people opt for doing data visualisations in their frontend framework of choice and using D3 only for math. Airbnb did this with their flexible visualisation library [visx](https://airbnb.io/visx/).

 I think we can do better with Clojure. My observation is that a lot of these libraries are offering conveniences to avoid writing boilerplate which is avoided in a more concise language.


There has been efforts to make data visualisation and interactive visuals in Clojure and Clojurescript.

Clojurescript/Clojure
- [Oz](https://github.com/metasoarous/oz) - Uses vega and vega-lite
- [svg-clj](https://github.com/adam-james-v/svg-clj) - svg abstractions
- [Hanami](https://github.com/jsa-aerial/hanami) - vega and vega-list
- [Quil](https://github.com/quil/quil) - processing and p5.js
- [thi.ng/geom](https://github.com/thi-ng/geom)
- [C2](https://github.com/lynaghk/c2) - archived

JVM Only
- [Darkstar](https://github.com/applied-science/darkstar)
- [cljplot](https://github.com/generateme/cljplot)
- [Dali](https://github.com/stathissideris/dali)

More can be found at [scicloj](https://scicloj.github.io/docs/resources/libs/#data-visualization-libraries)

---
I find that such abstractions often get in the way. The best abstractions are the ones we write for ourselves. But to do that we need to know how to play with the raw materials. This gallery is a collection of demos from the [D3 Gallery](https://observablehq.com/@d3/gallery?utm_source=d3js-org&utm_medium=hero&utm_campaign=try-observable) using as little as possible libraries that abstract over the ground level semantics of working with SVG.

I hope that this can become a refernce point of how to perform certain datavisualisations from scratch. We can also have [React](https://electric.hyperfiddle.net/user.demo-reagent-interop!ReagentInterop) versions by doing interop from Electric. From this point of reference only then can we see if a generic layer of abstraction provides the right level of trade-offs in flexibility, readability and code reduction.

---

Below is a continuation of the relevant docs to get started with this repo from the Electric Clojure starter app readme.



```
$ clj -A:dev -X user/main

Starting Electric compiler and server...
shadow-cljs - server version: 2.20.1 running at http://localhost:9630
shadow-cljs - nREPL server started on port 9001
[:app] Configuring build.
[:app] Compiling ...
[:app] Build completed. (224 files, 0 compiled, 0 warnings, 1.93s)

👉 App server available at http://0.0.0.0:8080
```

# Error reporting

Reproduce this now and confirm error handling works so you trust it:

![screenshot of electric error reporting](readme-electric-error-reporting-proof.png)

Electric is a reactive (async) language. Like React.js, we reconstruct synthetic async stack traces. If you aren't seeing them, something is wrong!

# Logging

The Electric server logs. The default logger config is slightly verbose by default to force you to see it working:

```
DEBUG hyperfiddle.electric.impl.env: reloading app.todo-list
DEBUG hyperfiddle.electric-jetty-adapter: Client disconnected for an unknown reason (browser default close code) {:status 1005, :reason nil}
DEBUG hyperfiddle.electric-jetty-adapter: Websocket handler completed gracefully.
DEBUG hyperfiddle.electric-jetty-adapter: WS connect ...
DEBUG hyperfiddle.electric.impl.env: reloading app.todo-list
DEBUG hyperfiddle.electric-jetty-adapter: Client disconnected for an unknown reason (browser default close code) {:status 1005, :reason nil}
```

**Silence the Electric debug logs by live editing logback.xml** and setting `name="hyperfiddle"` to `level="INFO"`, it will hot code reload so no restart is needed. Please **do NOT disable logs entirely**; the Electric server logs one important warning at the `INFO` level we call **unserializable reference transfer**, here is an example:

```
(e/defn TodoCreate []
  (e/client
    (InputSubmit. (e/fn [v]
                    (e/server
                      (d/transact! !conn [{:task/description v
                                           :task/status :active}])
                      nil))))) ;     <-- here
```

Note the intentional `nil` in the final line. If you remove the nil — try it right now — Electric will attempt to serialize whatever `d/transact!` returns — a reference — and stream it to the client. Since that reference cannot be serialized, Electric will send `nil` instead, and log at the `INFO` level:

```
INFO  hyperfiddle.electric.impl.io: Unserializable reference transfer: datascript.lru$cache$reify__35945 datascript.lru$cache$reify__35945@48ea0f24
INFO  hyperfiddle.electric.impl.io: Unserializable reference transfer: datascript.db.Datom #datascript/Datom [1 :task/description "asdf" 536870913 true]
...
```

We decided not to throw an exception here because it is almost always unintentional when this happens. **Do not disable this warning, it will save you one day!** If you want to target this exact message, use this:
`<logger name="hyperfiddle.electric.impl.io" level="DEBUG" additivity="false"><appender-ref ref="STDOUT" /></logger>`

[Note: Perhaps we should revisit this decision in the future now that our exception handling is more mature.]

