import de.hybris.platform.core.Registry
import de.hybris.platform.core.model.ItemModel
import de.hybris.platform.jalo.Item
import de.hybris.platform.jalo.type.ComposedType

def modelService = Registry.getApplicationContext().getBean("modelService")

def productTypesGenerator = Registry.getApplicationContext().getBean("productComposedTypesProducer")
def contentTypesGenerator = Registry.getApplicationContext().getBean("contentComposedTypesProducer")

def productTypesSet = productTypesGenerator.generateComposedTypes()
def contentTypeSet = contentTypesGenerator.generateComposedTypes()

removeComposedTypes(productTypesSet)
removeComposedTypes(contentTypeSet)

def removeComposedTypes(typesSet) {
    println(((ComposedType) typesSet.iterator().next()).getAllInstances().size());
    for (ComposedType type : typesSet) {

        println("***********************************")
        println("composed type: " + type.getCode())

        if (!type.getCode().equals("CMSPageType")) {
            removeItemsWitSubTypes(type)
        }
    }
}

def removeItemsWitSubTypes(type) {

    println("Remove TYPE: " + type.getCode())

    if (type.getSubTypes().size() > 0) {
        for (ComposedType subType : type.getSubTypes()) {
            println("Remove subtype: " + subType.getCode())
            removeItemsWitSubTypes(subType)
        }
        removeTypeInstances(type)
    } else {
        //Set<ItemModel> modelsToRemove = new HashSet<>()
        println("size of instances :" + type.getAllInstances().size())
        removeTypeInstances(type)
    }
}

def removeTypeInstances(type) {

    for (Object item : type.getAllInstances()) {

        println("item instanceof ComposedType: " + item instanceof ComposedType)

        if (item instanceof Item && !(item instanceof ComposedType)) {

            ItemModel itemModel = modelService.get(item.getPK());

            println("Remove item fot type: " + type.getCode())
            println("Remove item: " + item.getAttribute("pk"))

            try {
                if (!modelService.isRemoved(itemModel)) {
                    println("Removing item for type: " + type.getCode())
                    modelService.remove(itemModel)
                }
            }

            catch (Exception e) {
                println("Can't remove item fot type: " + type.getCode())
            }
        }
    }
}


