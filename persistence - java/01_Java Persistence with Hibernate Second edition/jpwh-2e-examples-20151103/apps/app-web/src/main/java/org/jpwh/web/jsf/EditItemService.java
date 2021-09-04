package org.jpwh.web.jsf;

import org.jpwh.web.dao.ImageDAO;
import org.jpwh.web.dao.ItemDAO;
import org.jpwh.web.dao.UserDAO;
import org.jpwh.web.model.Image;
import org.jpwh.web.model.ImageLookup;
import org.jpwh.web.model.Item;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Part;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.math.BigDecimal;

import static javax.enterprise.event.Reception.IF_EXISTS;

@Named
/* 
    The service instance is conversation scoped. By default, however,
    the conversation context is transient, and therefore behaves as a
    request-scoped service.
 */
@ConversationScoped
/* 
    The class must be <code>Serializable</code>, unlike a request-scope
    implementation. An instance of <code>EditItemService</code> might
    be stored in the HTTP session, and that session data might be
    serialized to disk or sent across the network in a cluster. We
    took the easy way out in the previous chapter using a stateful
    EJB, saying: "it's not passivation capable". Anything in the
    CDI conversation scope however <em>must</em> be passivation-capable
    and therefore serializable.
 */
public class EditItemService implements Serializable {

    /* 
        The injected DAO instances have dependent scope and are serializable. You
        might think they are not, because they have an <code>EntityManager</code>
        field, which is not serializable. We'll talk about this mismatch
        in a second.
     */
    @Inject
    ItemDAO itemDAO;

    @Inject
    ImageDAO imageDAO;

    @Inject
    UserDAO userDAO;

    /* 
        The <code>Conversation</code> API provided by the container, call it
        to control the conversation context. You'll need it when the user
        clicks on the <em>Next</em> button for the first time, promoting the
        transient conversation to long-running.
     */
    @Inject
    Conversation conversation;

    /* 
        This is the state of the service: the item you are editing
        on the pages of the wizard. You start with a fresh <code>Item</code>
        entity instance in transient state. If this service is initialized
        with an item identifier value, load the <code>Item</code> in
        <code>setItemId()</code>.
     */
    Long itemId;
    Item item = new Item();

    /* 
        This is some transient state of the service; we only need it
        temporarily when the user clicks the <em>Upload</em> button on the
        "Edit Images" page. The <code>Part</code> class of the Servlet
        API is not serializable. It's not uncommon to have some
        transient state in a conversational service, but you must
        initialize it for every request when it's needed.
     */
    transient Part imageUploadPart;

    /* 
        The <code>setItemId</code> method will only be called if the
        request contains an item identifier value. You therefore have two
        entry points into this conversation: With or without an existing item's
        identifier value.
     */
    public void setItemId(Long itemId) {
        this.itemId = itemId;
        if (item.getId() == null && itemId != null) {
            /* 
                If you are editing an item, you must load it from
                the database. You are still relying on a
                request-scoped persistence context, so as soon
                as the request is complete, this <code>Item</code>
                instance is in detached state. You can hold
                detached entity instances in a conversational service's
                state and merge it when needed to persist changes.
             */
            item = itemDAO.findById(itemId);
            if (item == null)
                throw new EntityNotFoundException();
        }
    }

    public Conversation getConversation() {
        return conversation;
    }

    public Long getItemId() {
        return itemId;
    }

    public Item getItem() {
        return item;
    }

    public Part getImageUploadPart() {
        return imageUploadPart;
    }

    public void setImageUploadPart(Part imageUploadPart) {
        this.imageUploadPart = imageUploadPart;
    }

    public String editImages() {
        if (conversation.isTransient()) {
            conversation.setTimeout(10 * 60 * 1000); // 10 minutes
            conversation.begin();
        }
        return "editItemImages";
    }

    public void uploadImage() throws Exception {
        if (imageUploadPart == null)
            return;

        /* 
            Create the <code>Image</code> entity instance from the
            submitted multi-part form.
         */
        Image image =
            imageDAO.hydrateImage(imageUploadPart.getInputStream());
        image.setName(imageUploadPart.getSubmittedFileName());
        image.setContentType(imageUploadPart.getContentType());

        /* 
            You must add the transient <code>Image</code> to the
            transient or detached <code>Item</code>. This
            conversation will consume more and more memory on
            the server, as uploaded image data is added to conversational
            state and therefore the user's session.
         */
        image.setItem(item);
        item.getImages().add(image);
    }

    /* 
        The system transaction interceptor wraps the method call.
     */
    @Transactional
    public String submitItem() {
        /* 
            You must join the unsynchronized request-scoped persistence context
            with the system transaction if you want to store data.
         */
        itemDAO.joinTransaction();

        item.setSeller(userDAO.findById(1l));

        /* 
            This DAO call will make the transient or detached <code>Item</code>
            persistent, and because you enabled it with a cascading rule on the
            <code>@OneToMany</code>, also store any new transient or old
            detached <code>Item#images</code> collection elements. According
            to the DAO contract, you must take the returned instance as the "current"
            state.
         */
        item = itemDAO.makePersistent(item);

        /* 
            You manually end the long-running conversation. This is effectively a
            demotion: The long-running conversation becomes transient; you destroy
            the conversation context and this service instance when the request is
            complete. All conversational state is removed from the user's session.
         */
        if (!conversation.isTransient())
            conversation.end();

        /* 
            This is a redirect-after-POST in JSF to the auction item details page,
            with the new identifier value of the now persistent <code>Item</code>.
         */
        return "auction?id=" + item.getId() + "&faces-redirect=true";
    }

    public String cancel() {
        if (!conversation.isTransient())
            conversation.end();
        return "catalog?faces-redirect=true";
    }

    // A nice trick with CDI: This bean will answer if the lookup event is fired by
    // someone, but it won't be created if it doesn't exist already.
    public void getConversationalImage(@Observes(notifyObserver = IF_EXISTS)
                                       ImageLookup imageLookup) {
        // We might have transient images without identifier value, so we assume
        // the lookup identifier is actually the index of an image in our list of images
        Image image = getItem().getImagesSorted().get(
            new BigDecimal(imageLookup.getId()).intValueExact()
        );
        if (image != null)
            imageLookup.setImage(image);
    }
}