public class ReactorBdioDocument extends BdioDocument {

    public ReactorBdioDocument(BdioContext context) {
        super(context);
    }

    @Override
    public Flux<Object> read(InputStream in) {
        return Flux.generate(
                () -> EmitterFactory.newEmitter(context(), in),
                (parser, emitter) -> {
                    parser.emit(emitter::next, emitter::error, emitter::complete);
                    return parser;
                },
                Emitter::dispose);
    }

    @Override
    public Subscriber<Object> write(BdioMetadata metadata, StreamSupplier entryStreams) {
        EmitterProcessor<Object> data = EmitterProcessor.create();

        jsonLd(data)
                .expand()
                .flatMapIterable(BdioDocument::toGraphNodes)
                .subscribe(new BdioSubscriber(metadata, entryStreams, t -> {})); // TODO Generic error handler?

        return data;
    }

    @Override
    public ReactorJsonLdProcessing jsonLd(Publisher<Object> inputs) {
        return new ReactorJsonLdProcessing(Flux.from(inputs), context().jsonLdOptions());
    }

    @Override
    public Flux<BdioMetadata> metadata(Publisher<Object> inputs) {
        return jsonLd(Flux.from(inputs).map(BdioDocument::toMetadata))
                .expand().flatMapIterable(BdioDocument::unfoldExpand)
                .onErrorResume(t -> Flux.just(new BdioMetadata(t)))
                .reduce(new BdioMetadata(), BdioMetadata::merge)
                .flux();
    }

}
