const urlLoader = Packages.java.net.URLClassLoader([
    Vars.mods.getMod(modName).file.file().toURI().toURL()
], Vars.mods.mainLoader());
function getClass(name){
	return Packages.rhino.NativeJavaClass(Vars.mods.scripts.scope, urlLoader.loadClass(name));
};
const MultiCrafter = getClass("multicraft.MultiCrafter")


const c = MultiCrafter("standalone")
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

print(">>>>>MultiCrafter Standalone JavaScript loaded.")
