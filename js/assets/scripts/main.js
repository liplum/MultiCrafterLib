function setup(block){
    block.requirements = ItemStack.with(
        Items.graphite, 5,
        Items.silicon, 3
    )
    block.health = 110;
    block.buildVisibility = BuildVisibility.shown
    block.category = Category.crafting
}

const multi = require("multi-crafter/lib")
const c = multi.MultiCrafter("js")
setup(c)
c.recipes = [{
    input:{
        items : [
        "copper/1",{
            item:"surge-alloy",
            amount: 2
        }]
    },
    output:{
        items : ["coal/1"],
        power : 2
    },
    craftTime : 120.0
},{
    input:{
        items : [
        "cyber-io-ic/1",
        "titanium/1"
        ],
        power : 2
    },
    output:{
        items : "graphite/1",
        fluids : "cyber-io-cyberion/1.2"
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
},{
    input:{
        heat : 8
    },
    output: "sand/1",
    craftTime : 120.0
}]

print(">>>>>MultiCrafter Test JavaScript loaded.")

const inHjson = multi.MultiCrafter("in-hjson")
setup(inHjson)
