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
const testCrafter = extend(GenericCrafter, "test-crafter", {})
setup(testCrafter)
testCrafter.consumePower(1.0)
testCrafter.consumeItem(Items.coal, 1)
testCrafter.outputItem = ItemStack(Items.surgeAlloy,1)

const multi= require("multi-crafter/lib")
const multiCrafter = multi.MultiCrafter("test-multi-crafter")
setup(multiCrafter)

print(">>>>>MultiCrafter Test JavaScript loaded.")
