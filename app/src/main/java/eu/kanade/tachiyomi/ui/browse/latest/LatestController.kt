package eu.kanade.tachiyomi.ui.browse.latest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.database.models.Manga
import eu.kanade.tachiyomi.data.preference.asImmediateFlow
import eu.kanade.tachiyomi.databinding.LatestControllerBinding
import eu.kanade.tachiyomi.source.CatalogueSource
import eu.kanade.tachiyomi.ui.base.controller.NucleusController
import eu.kanade.tachiyomi.ui.base.controller.withFadeTransaction
import eu.kanade.tachiyomi.ui.browse.source.latest.LatestUpdatesController
import eu.kanade.tachiyomi.ui.manga.MangaController
import kotlinx.coroutines.flow.launchIn

/**
 * This controller shows and manages the different search result in global search.
 * This controller should only handle UI actions, IO actions should be done by [LatestPresenter]
 * [LatestCardAdapter.OnMangaClickListener] called when manga is clicked in global search
 */
open class LatestController :
    NucleusController<LatestControllerBinding, LatestPresenter>(),
    LatestCardAdapter.OnMangaClickListener,
    LatestAdapter.OnTitleClickListener {

    /**
     * Adapter containing search results grouped by lang.
     */
    protected var adapter: LatestAdapter? = null

    /*init {
        setHasOptionsMenu(true)
    }*/

    /**
     * Initiate the view with [R.layout.global_search_controller].
     *
     * @param inflater used to load the layout xml.
     * @param container containing parent views.
     * @return inflated view
     */
    override fun inflateView(inflater: LayoutInflater, container: ViewGroup): View {
        binding = LatestControllerBinding.inflate(inflater)
        return binding.root
    }

    override fun getTitle(): String? {
        return applicationContext?.getString(R.string.latest)
    }

    /**
     * Create the [LatestPresenter] used in controller.
     *
     * @return instance of [LatestPresenter]
     */
    override fun createPresenter(): LatestPresenter {
        return LatestPresenter()
    }

    /**
     * Called when manga in global search is clicked, opens manga.
     *
     * @param manga clicked item containing manga information.
     */
    override fun onMangaClick(manga: Manga) {
        // Open MangaController.
        parentController?.router?.pushController(MangaController(manga, true).withFadeTransaction())
    }

    /**
     * Called when manga in global search is long clicked.
     *
     * @param manga clicked item containing manga information.
     */
    override fun onMangaLongClick(manga: Manga) {
        // Delegate to single click by default.
        onMangaClick(manga)
    }

    /**
     * Adds items to the options menu.
     *
     * @param menu menu containing options.
     * @param inflater used to load the menu xml.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate menu.
        /*inflater.inflate(R.menu.global_search, menu)

        // Initialize search menu
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.onActionViewExpanded() // Required to show the query in the view
                searchView.setQuery(presenter.query, false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                return true
            }
        })*/
    }

    /**
     * Called when the view is created
     *
     * @param view view of controller
     */
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter = LatestAdapter(this)

        // Create recycler and set adapter.
        binding.recycler.layoutManager = LinearLayoutManager(view.context)
        binding.recycler.adapter = adapter

        presenter.preferences.latestTabSources()
            .asImmediateFlow { presenter.getLatest() }
            .launchIn(scope)
    }

    override fun onDestroyView(view: View) {
        adapter = null
        super.onDestroyView(view)
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        adapter?.onSaveInstanceState(outState)
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)
        adapter?.onRestoreInstanceState(savedViewState)
    }

    /**
     * Returns the view holder for the given manga.
     *
     * @param source used to find holder containing source
     * @return the holder of the manga or null if it's not bound.
     */
    private fun getHolder(source: CatalogueSource): LatestHolder? {
        val adapter = adapter ?: return null

        adapter.allBoundViewHolders.forEach { holder ->
            val item = adapter.getItem(holder.bindingAdapterPosition)
            if (item != null && source.id == item.source.id) {
                return holder as LatestHolder
            }
        }

        return null
    }

    /**
     * Add search result to adapter.
     *
     * @param latestManga the source items containing the latest manga.
     */
    fun setItems(latestManga: List<LatestItem>) {
        adapter?.updateDataSet(latestManga)

        if (latestManga.isEmpty()) {
            binding.emptyView.show(R.string.latest_tab_empty)
        } else {
            binding.emptyView.hide()
        }
    }

    /**
     * Called from the presenter when a manga is initialized.
     *
     * @param manga the initialized manga.
     */
    fun onMangaInitialized(source: CatalogueSource, manga: Manga) {
        getHolder(source)?.setImage(manga)
    }

    /**
     * Opens a catalogue with the given search.
     */
    override fun onTitleClick(source: CatalogueSource) {
        presenter.preferences.lastUsedSource().set(source.id)
        parentController?.router?.pushController(LatestUpdatesController(source).withFadeTransaction())
    }
}
