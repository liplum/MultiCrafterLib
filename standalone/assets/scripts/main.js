print(">>>>>MultiCrafter Standalone JavaScript loaded.")
const multi = require("multi-crafter/lib")
const c = multi.MultiCrafter("standalone")
c.requirements = ItemStack.with(
    Items.graphite, 5,
    Items.silicon, 3
)
c.health = 110;
c.buildVisibility = BuildVisibility.shown
c.category = Category.crafting
c.recipes = [{
    input:{
        items : [
          "titanium/1"
        ],
        power : 2
    },
    output:{
        items : "graphite/1",
    },
    craftTime : 240.0
},{
    input:{
        fluids : "water/1"
    },
    output:{
        fluids:{
            fluid : "slag",
            amount : 1.5
        }
    },
    craftTime : 240.0
},{
    input:{
        fluids : "water/1"
    },
    output:{
        heat : 5
    },
    craftTime : 120.0
}]