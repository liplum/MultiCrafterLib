function setup(block){
    block.requirements = ItemStack.with(
        Items.lead, 8,
        Items.graphite, 5,
        Items.silicon, 3
    )
    block.health = 110;
    block.buildVisibility = BuildVisibility.shown
    block.category = Category.crafting
}
const multi = require("multi-crafter/lib")
const c = multi.MultiCrafter("test-multi-crafter")
setup(c)
c.recipes = [
{
    input:{
        items : ["copper/1"]
    },
    output:{
        items : ["coal/1"]
    },
    craftTime : 120.0
}
]

print(">>>>>MultiCrafter Test JavaScript loaded.")
