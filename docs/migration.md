# Migration Guide

This is a guide to help users migrate from the `v1.y.z` to `v2.y.z`.

???+ note
    The `v2.y.z` is not yet officially out and is still in the devlopment phase!

## Nullary Constructor

The class `Recipe` and `IOEntry` are now using nullary constructor.
```java
new Recipe() {{
    input = new IOEntry() {{
        ...
    }};
    output = new IOEntry() {{
        ...
    }};
    craftTime = 120f;
}}
```

<!-- === "Dependency"
    === "JSON"
        yes

    === "JavaScript"
        h

    === "Java"
        i

=== "Injection"
    === "JSON"
        yes

    === "JavaScript"
        h

    === "Java"
        i

=== "Jitpack"
    ???+ info
        This is Java only! -->